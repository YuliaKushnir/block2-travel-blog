package org.example.block2travelblog.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.block2travelblog.data.Post;
import org.example.block2travelblog.data.User;
import org.example.block2travelblog.dto.*;
import org.example.block2travelblog.exception.CreationException;
import org.example.block2travelblog.repository.PostRepository;
import org.example.block2travelblog.repository.UserRepository;
import org.example.block2travelblog.repository.specification.PostSpecifications;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Service implementation for posts operations.
 */
@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final ObjectMapper objectMapper;

    /**
     * Creates a new post
     *
     * @param savePostDto post data
     * @return created post dto
     * @throws EntityNotFoundException if user not found
     */
    public PostDto savePost(SavePostDto savePostDto) {
        User user = userRepository.findById(savePostDto.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found for id: " + savePostDto.getUserId()));

        Post post = new Post();
        BeanUtils.copyProperties(savePostDto, post);
        post.setCategories(savePostDto.getCategories());
        post.setCreatedAt(LocalDate.now());
        post.setRating(0.0);
        post.setUser(user);

        Post createdPost = postRepository.save(post);

        if (createdPost == null || createdPost.getId() == null) {
            throw new CreationException("Failed to create post");
        }

        return mapPostToPostDto(createdPost);
    }

    /**
     * Retrieves post data including user data
     *
     * @param id post id
     * @return post data
     * @throws EntityNotFoundException if post not found by id
     */
    public ExtendedPostDto getPostById(Long id) {
        Post post = postRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Post not found for id: " + id));

        UserDto userDto = new UserDto();
        userDto.setId(post.getUser().getId());
        userDto.setName(post.getUser().getName());
        userDto.setEmail(post.getUser().getEmail());

        ExtendedPostDto extendedPostDto = new ExtendedPostDto();
        BeanUtils.copyProperties(post, extendedPostDto);
        extendedPostDto.setCategory(post.getCategories());
        extendedPostDto.setAuthor(userDto);

        return extendedPostDto;
    }


    /**
     * Updates an existing post
     *
     * @param id post id
     * @param savePostDto post data
     * @return updated post
     * @throws EntityNotFoundException if post not found by id
     */
    public PostDto updatePost(Long id, SavePostDto savePostDto) {
        Post post = postRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Post not found for id: " + id));

        post.setTitle(savePostDto.getTitle());
        post.setContent(savePostDto.getContent());
        post.setCountry(savePostDto.getCountry());
        post.setCategories(savePostDto.getCategories());

        Post updatedPost = postRepository.save(post);

        return mapPostToPostDto(updatedPost);
    }

    /**
     * Deletes post by id
     *
     * @param id post id
     * @throws EntityNotFoundException if post not found with id
     */
    public void deletePost(Long id) {
        if(!postRepository.existsById(id)) {
            throw new EntityNotFoundException("Post not found for id: " + id);
        }
        postRepository.deleteById(id);
    }

    /**
     * Retrieves page of filtered posts
     *
     * @param postQueryDto filtering fields, page size, page number
     * @return FilteredPostResponse with list of posts, total number of pages and total count of elements
     */
    public FilteredPostResponse search(PostQueryDto postQueryDto) {
        Pageable pageable = PageRequest.of(postQueryDto.getFrom(), postQueryDto.getSize());

        Specification<Post> specification = buildSpecification(postQueryDto);

        Page<PostDto> dtoPage = postRepository.findAll(specification, pageable)
                .map(this::mapPostToPostDto);

        return new FilteredPostResponse(
                dtoPage.getContent(),
                dtoPage.getTotalPages(),
                dtoPage.getTotalElements()
        );
    }

    /**
     * Generates file .xlsx with filtered posts
     *
     * @param response
     * @param postFilteredFieldsDto fields for filtering
     */
    public void generateReport(HttpServletResponse response, PostFilteredFieldsDto postFilteredFieldsDto) {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=posts.xlsx");

        List<Post> posts = postRepository.findAll(buildSpecification(postFilteredFieldsDto));

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Posts");

            createHeaderRow(sheet);
            fillDataRows(sheet, posts);

            autoSizeColumns(sheet);

            workbook.write(response.getOutputStream());
            response.flushBuffer();
        } catch (IOException e) {
            throw new RuntimeException("Error generating XLSX report", e);
        }
    }

    /**
     * Uploads post data from .json file
     *
     * @param file with posts data
     * @return number of successful and failed saved posts
     */
    public UploadedPostsRestResponse uploadFromFile(MultipartFile file) {
        int successfulCount = 0;
        int failedCount = 0;

        if (!isValidJsonFile(file)) {
            throw new RuntimeException("Invalid JSON file format");
        }

        try {
            byte[] fileBytes = file.getBytes();
            List<PostDto> postDtoList = objectMapper.readValue(fileBytes, new TypeReference<List<PostDto>>() {});

            List<Post> validPosts = new ArrayList<>();
            for (PostDto postDto : postDtoList) {
                if (!hasValidPostFields(postDto)) {
                    failedCount++;
                    continue;
                }

                try{
                    Post post = convertFromUpload(postDto);
                    validPosts.add(post);
                    successfulCount++;
                } catch(EntityNotFoundException e){
                    failedCount++;
                }
            }

            postRepository.saveAll(validPosts);

        } catch(IOException e) {
            throw new RuntimeException("Error uploading file", e);
        }

        return new UploadedPostsRestResponse("Posts successfully uploaded from file: " + successfulCount,
                "Posts uploading failed: " + failedCount);
    }

    private Specification<Post> buildSpecification(PostFilter filter) {
        return Specification
                .where(PostSpecifications.hasCountry(filter.getCountry()))
                .and(PostSpecifications.hasCategory(filter.getCategory()))
                .and(PostSpecifications.isCreatedAfter(filter.getCreatedAfter()))
                .and(PostSpecifications.hasMinRating(filter.getMinRating()))
                .and(PostSpecifications.hasUserId(filter.getUserId()));
    }

    private void createHeaderRow(Sheet sheet) {
        Row headerRow = sheet.createRow(0);
        String[] columns = {"id", "title", "content", "country", "category", "createdAt", "rating", "userId"};
        for (int i = 0; i < columns.length; i++) {
            headerRow.createCell(i).setCellValue(columns[i]);
        }
    }

    private void fillDataRows(Sheet sheet, List<Post> posts) {
        int rowNum = 1;
        for (Post post : posts) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(post.getId());
            row.createCell(1).setCellValue(post.getTitle());
            row.createCell(2).setCellValue(post.getContent());
            row.createCell(3).setCellValue(post.getCountry());

            String categories = post.getCategories() != null ? String.join(", ", post.getCategories()) : "";
            row.createCell(4).setCellValue(categories);

            row.createCell(5).setCellValue(post.getCreatedAt().toString());
            row.createCell(6).setCellValue(post.getRating());
            row.createCell(7).setCellValue(post.getUser().getId());
        }
    }

    private void autoSizeColumns(Sheet sheet) {
        int totalColumns = sheet.getRow(0).getPhysicalNumberOfCells();
        for (int i = 0; i < totalColumns; i++) {
            if (i == 2) continue; // content може бути дуже довгим
            sheet.autoSizeColumn(i);
        }
    }


    private Post convertFromUpload(PostDto postDto) {
        Post post = new Post();

        post.setTitle(postDto.getTitle());
        post.setContent(postDto.getContent());
        post.setCountry(postDto.getCountry());
        post.setCategories(postDto.getCategories());
        post.setCreatedAt(postDto.getCreatedAt());
        post.setRating(postDto.getRating());

        post.setUser(userRepository.findById(postDto.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found for id: " + postDto.getUserId())));

        return post;
    }

    private PostDto mapPostToPostDto(Post post){
        PostDto postDto = new PostDto();
        postDto.setId(post.getId());
        postDto.setTitle(post.getTitle());
        postDto.setContent(post.getContent());
        postDto.setCountry(post.getCountry());
        postDto.setCategories(post.getCategories());
        postDto.setCreatedAt(post.getCreatedAt());
        postDto.setRating(post.getRating());
        postDto.setUserId(post.getUser().getId());
        return postDto;
    }

    private boolean isValidJsonFile(MultipartFile file) {
        try {
            byte[] fileBytes = file.getBytes();
            objectMapper.readValue(fileBytes, new TypeReference<List<PostDto>>() {});
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private boolean hasValidPostFields(PostDto postDto) {
        return postDto.getTitle() != null && !postDto.getTitle().isBlank()
                && postDto.getContent() != null && postDto.getContent().length() >= 10
                && postDto.getCountry() != null && !postDto.getCountry().isBlank()
                && postDto.getCategories() != null && !postDto.getCategories().isEmpty()
                && postDto.getCreatedAt() != null
                && postDto.getRating() != null
                && postDto.getUserId() != null;
    }

}
