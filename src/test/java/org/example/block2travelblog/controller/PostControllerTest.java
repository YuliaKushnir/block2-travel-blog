package org.example.block2travelblog.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.block2travelblog.Block2TravelBlogApplication;
import org.example.block2travelblog.data.Post;
import org.example.block2travelblog.data.User;
import org.example.block2travelblog.dto.*;
import org.example.block2travelblog.repository.PostRepository;
import org.example.block2travelblog.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = Block2TravelBlogApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PostControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PostRepository postRepository;

    @Autowired
    ObjectMapper objectMapper;

    @AfterEach
    void afterEach() {
        userRepository.deleteAll();
    }

    @Test
    void savePost_success() throws Exception {
        User user = saveUserInTestDb();

        String body = """
           {
                "title": "Test Title",
                "content": "Test Content",
                "country": "Ukraine",
                "categories": ["travel", "city"],
                "userId": %d
            }
            """.formatted(user.getId());


        MvcResult mvcResult = mvc.perform(post("/api/post")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();

        PostDto postDto = parseResponse(mvcResult, PostDto.class);
        assertThat(postDto.getId()).isNotNull();
        assertThat(postDto.getTitle()).isEqualTo("Test Title");
        assertThat(postDto.getContent()).contains("Test Content");
        assertThat(postDto.getCountry()).isEqualTo("Ukraine");
        assertThat(postDto.getCategories()).containsExactly("travel", "city");
        assertThat(postDto.getUserId()).isEqualTo(user.getId());
        assertThat(postDto.getRating()).isEqualTo(0.0);
        assertThat(postDto.getCreatedAt()).isEqualTo(LocalDate.now());
    }

    @Test
    void savePost_validation() throws Exception {
        String body = """
            {
                "title": "",
                "content": "short",
                "country": "",
                "categories": [],
                "userId": null
            }
        """;

        mvc.perform(post("/api/post")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void savePost_userNotFound() throws Exception {
        String body = """
            {
                "title": "My post",
                "content": "This is valid content with more than 10 chars",
                "country": "Ukraine",
                "categories": ["travel"],
                "userId": 999
            }
        """;

        mvc.perform(post("/api/post")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(result ->
                        assertThat(result.getResolvedException())
                                .isInstanceOf(EntityNotFoundException.class)
                );
    }

    @Test
    void getPostById_success() throws Exception {
        User user = saveUserInTestDb();
        Post post = savePostInTestDb(user);

        MvcResult mvcResult = mvc.perform(get("/api/post/{id}", post.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        ExtendedPostDto extendedPostDto = parseResponse(mvcResult, ExtendedPostDto.class);

        assertThat(extendedPostDto.getId()).isEqualTo(post.getId());
        assertThat(extendedPostDto.getTitle()).isEqualTo("Test Title");
        assertThat(extendedPostDto.getContent()).contains("Test Content");
        assertThat(extendedPostDto.getCountry()).isEqualTo("Ukraine");
        assertThat(extendedPostDto.getCategories()).containsExactly("travel", "city");
        assertThat(extendedPostDto.getRating()).isEqualTo(0.0);
        assertThat(extendedPostDto.getCreatedAt()).isEqualTo(LocalDate.now());

        assertThat(extendedPostDto.getAuthor().getId()).isEqualTo(user.getId());
        assertThat(extendedPostDto.getAuthor().getName()).isEqualTo("Marusia");
        assertThat(extendedPostDto.getAuthor().getEmail()).isEqualTo("marusia@test.com");
    }

    @Test
    void getPostById_notFound() throws Exception {

        mvc.perform(get("/api/post/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(result ->
                        assertThat(result.getResolvedException())
                                .isInstanceOf(EntityNotFoundException.class)
                );
    }

    @Test
    void testUpdatePost_success() throws Exception {
        User user = saveUserInTestDb();
        Post post = savePostInTestDb(user);

        String body = """
            {
                "title": "Updated title",
                "content": "Updated Content",
                "country": "Canada",
                "categories": ["photography", "adventure"],
                "userId": %d
            }
        """.formatted(user.getId());

        MvcResult mvcResult = mvc.perform(put("/api/post/{id}", post.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();

        PostDto updatedPost = parseResponse(mvcResult, PostDto.class);
        assertThat(updatedPost.getTitle()).isEqualTo("Updated title");
        assertThat(updatedPost.getContent()).contains("Updated Content");
        assertThat(updatedPost.getCountry()).isEqualTo("Canada");
        assertThat(updatedPost.getCategories()).containsExactly("photography", "adventure");
        assertThat(updatedPost.getUserId()).isEqualTo(user.getId());
    }

    @Test
    void updatePost_notFound() throws Exception {
        String body = """
            {
                "title": "Updated title",
                "content": "Updated Content",
                "country": "Canada",
                "categories": ["photography", "adventure"],
                "userId": 1
            }
        """;

        mvc.perform(put("/api/post/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(result ->
                        assertThat(result.getResolvedException())
                                .isInstanceOf(EntityNotFoundException.class)
                );
    }

    @Test
    void updatePost_validation() throws Exception {
        User user = saveUserInTestDb();
        Post post = savePostInTestDb(user);

        String body = """
            {
                "title": "",
                "content": "Short",
                "country": "",
                "categories": [""],
                "userId": %d
            }
        """.formatted(user.getId());

        mvc.perform(put("/api/post/{id}", post.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDeletePost_success() throws Exception {
        User user = saveUserInTestDb();
        Post post = savePostInTestDb(user);

        mvc.perform(delete("/api/post/{id}", post.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        assertThat(postRepository.existsById(post.getId())).isFalse();
    }

    @Test
    void testDeletePost_notFound() throws Exception {
        mvc.perform(delete("/api/post/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(result ->
                        assertThat(result.getResolvedException())
                                .isInstanceOf(EntityNotFoundException.class)
                );
    }

    @Test
    void testGetPostPage_success() throws Exception {
        User user = saveUserInTestDb();

        Post post1 = savePostInTestDb(user);

        Post post2 = new Post();
        post2.setTitle("Travel to Canada");
        post2.setContent("Test content about trip");
        post2.setCountry("Canada");
        post2.setCategories(List.of("city", "culture"));
        post2.setCreatedAt(LocalDate.now());
        post2.setRating(3.0);
        post2.setUser(user);
        postRepository.save(post2);

        String body = """
            {
                "country": "Ukraine",
                "size": 10,
                "from": 0
            }
        """;

        MvcResult mvcResult = mvc.perform(post("/api/post/_list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();

        FilteredPostResponse response = parseResponse(mvcResult, FilteredPostResponse.class);

        assertThat(response.getList()).hasSize(1);
        assertThat(response.getList().get(0).getTitle()).isEqualTo("Test Title");
        assertThat(response.getTotalPages()).isEqualTo(1);
        assertThat(response.getTotalElements()).isEqualTo(1);
    }

    @Test
    void testGetPostPage_emptyResult() throws Exception {

        String body = """
            {
                "country": "Ukraine",
                "size": 10,
                "from": 0
            }
        """;

        MvcResult mvcResult = mvc.perform(post("/api/post/_list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();

        FilteredPostResponse response = parseResponse(mvcResult, FilteredPostResponse.class);

        assertThat(response.getList()).isEmpty();
        assertThat(response.getTotalPages()).isEqualTo(0);
        assertThat(response.getTotalElements()).isEqualTo(0);
    }

    @Test
    void testGenerateReport_success() throws Exception {
        User user = saveUserInTestDb();
        Post post = savePostInTestDb(user);

        String body = """
          {
            "country": "Ukraine"
           }
        """;

        MvcResult mvcResult = mvc.perform(post("/api/post/_report")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=posts.xlsx"))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .andReturn();

        byte[] bytes = mvcResult.getResponse().getContentAsByteArray();
        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            Sheet sheet = workbook.getSheet("Posts");
            assertThat(sheet).isNotNull();

            Row headerRow = sheet.getRow(0);
            assertThat(headerRow.getCell(0).getStringCellValue()).isEqualTo("id");
            assertThat(headerRow.getCell(1).getStringCellValue()).isEqualTo("title");

            Row dataRow = sheet.getRow(1);
            assertThat(dataRow.getCell(1).getStringCellValue()).isEqualTo("Test Title");
            assertThat(dataRow.getCell(3).getStringCellValue()).isEqualTo("Ukraine");
        }
    }

    @Test
    void testGenerateReport_emptyResult() throws Exception {
        String body = """
          {
            "country": "Ukraine"
           }
        """;

        MvcResult mvcResult = mvc.perform(post("/api/post/_report")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=posts.xlsx"))
                .andReturn();

        byte[] bytes = mvcResult.getResponse().getContentAsByteArray();
        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            Sheet sheet = workbook.getSheet("Posts");
            assertThat(sheet).isNotNull();

            Row headerRow = sheet.getRow(0);
            assertThat(headerRow.getCell(0).getStringCellValue()).isEqualTo("id");

            assertThat(sheet.getPhysicalNumberOfRows()).isEqualTo(1);
        }
    }

    @Test
    void testUploadFromFile_success() throws Exception {
        User user = saveUserInTestDb();

        String json = """
        [
            {
                "title": "Test Title",
                "content": "Test Content",
                "country": "Ukraine",
                "categories": ["city", "culture"],
                "createdAt": "2025-12-01",
                "rating": 0.0,
                "userId": %d
            }
        ]
    """.formatted(user.getId());

        MockMultipartFile file = new MockMultipartFile(
                "file", "posts.json", "application/json", json.getBytes()
        );

        MvcResult mvcResult = mvc.perform(multipart("/api/post/file/upload")
                        .file(file))
                .andExpect(status().isCreated())
                .andReturn();

        UploadedPostsRestResponse response = parseResponse(mvcResult, UploadedPostsRestResponse.class);
        assertThat(response.getSuccessMessage()).contains("1");
        assertThat(response.getErrorMessage()).contains("0");

        assertThat(postRepository.findAll()).hasSize(1);
    }

    @Test
    void testUploadFromFile_invalidJson() throws Exception {
        String json = """
        [
            {
                "title": "Test Title",
                "content": "Test Content",
                "country": "Ukraine",
                "categories": ["city", "culture"],
                "createdAt": "2025-12-01",
                "rating": 0.0,
                "userId": 999
            }
        ]
        """;

        MockMultipartFile file = new MockMultipartFile(
                "file", "posts.json", "application/json", json.getBytes()
        );

        MvcResult mvcResult = mvc.perform(multipart("/api/post/file/upload")
                        .file(file))
                .andExpect(status().isCreated())
                .andReturn();

        UploadedPostsRestResponse response = parseResponse(mvcResult, UploadedPostsRestResponse.class);
        assertThat(response.getSuccessMessage()).contains("0");
        assertThat(response.getErrorMessage()).contains("1");
    }


    private <T>T parseResponse(MvcResult mvcResult, Class<T> c) {
        try {
            return objectMapper.readValue(mvcResult.getResponse().getContentAsString(), c);
        } catch (JsonProcessingException | UnsupportedEncodingException e) {
            throw new RuntimeException("Error parsing json", e);
        }
    }

    private User saveUserInTestDb(){
        User user = new User();
        user.setName("Marusia");
        user.setEmail("marusia@test.com");
        user.setPassword("12345678");
        return userRepository.save(user);
    }

    private Post savePostInTestDb(User user){
        Post post = new Post();
        post.setTitle("Test Title");
        post.setContent("Test Content");
        post.setCountry("Ukraine");
        post.setCategories(List.of("travel", "city"));
        post.setCreatedAt(LocalDate.now());
        post.setRating(0.0);
        post.setUser(user);
        return postRepository.save(post);
    }

}