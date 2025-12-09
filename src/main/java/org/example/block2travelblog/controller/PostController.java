package org.example.block2travelblog.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.example.block2travelblog.dto.*;
import org.example.block2travelblog.service.PostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * REST controller for Posts operations.
 */

@RestController
@RequestMapping("/api/post")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    /**
     * Creates a new post
     *
     * @param savePostDto
     * @return created post dto
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PostDto savePost(@Valid @RequestBody SavePostDto savePostDto) {
        return postService.savePost(savePostDto);
    }

    /**
     * Retrieves post by id
     *
     * @param id post ID
     * @return post extended details
     */
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ExtendedPostDto getPostById(@PathVariable Long id) {
        return postService.getPostById(id);
    }

    /**
     * Updates an existing post
     *
     * @param id post ID
     * @param savePostDto updated post data
     * @return updated post
     */
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public PostDto updatePost(
            @PathVariable @NotNull @Min(1) Long id,
            @Valid @RequestBody SavePostDto savePostDto) {

        return postService.updatePost(id, savePostDto);
    }

    /**
     * Deletes a post
     *
     * @param id post ID
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePost(@PathVariable Long id) {
        postService.deletePost(id);
    }

    /**
     * Retrieves page with filtered posts
     *
     * @param postQueryDto filtered fields, size, from
     * @return list of filtered posts, page number and page size
     */
    @PostMapping("/_list")
    @ResponseStatus(HttpStatus.OK)
    public FilteredPostResponse getPostPage(@RequestBody PostQueryDto postQueryDto) {
        return postService.search(postQueryDto);
    }

    /**
     * Generates a report with filtered posts in xlsx format
     *
     * @param response
     * @param postFilteredFieldsDto
     */
    @PostMapping(value="/_report", produces= MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void generateReport(HttpServletResponse response, @RequestBody PostFilteredFieldsDto postFilteredFieldsDto) {
        postService.generateReport(response, postFilteredFieldsDto);
    }

    /**
     * Upload post data from json file
     *
     * @param file
     * @return message with count of successful saving posts into db and count of failed saving
     */
    @PostMapping("/file/upload")
    @ResponseStatus(HttpStatus.CREATED)
    public UploadedPostsRestResponse uploadFromFile(@RequestParam("file") MultipartFile file) {
        return postService.uploadFromFile(file);
    }

}
