package org.example.block2travelblog.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.example.block2travelblog.Block2TravelBlogApplication;
import org.example.block2travelblog.data.User;
import org.example.block2travelblog.dto.UserDto;
import org.example.block2travelblog.exception.DuplicateEmailException;
import org.example.block2travelblog.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = Block2TravelBlogApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ObjectMapper objectMapper;

    @BeforeEach
    void beforeEach() {
        userRepository.deleteAll();
    }
    @AfterEach
    void afterEach() {
        userRepository.deleteAll();
    }

    @Test
    void testGetAllUsers_success() throws Exception {
        userRepository.deleteAll();
        User user = saveUserInTestDb();

        MvcResult mvcResult = mvc.perform(get("/api/user")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        List<UserDto> users = Arrays.asList(parseResponse(mvcResult, UserDto[].class));
        assertThat(users).hasSize(1);
        assertThat(users.get(0).getName()).isEqualTo("Marusia");
        assertThat(users.get(0).getEmail()).isEqualTo("marusia@test.com");
    }

    @Test
    void testGetAllUsers_notFound() throws Exception {
        mvc.perform(get("/api/user")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(result ->
                        assertThat(result.getResolvedException())
                                .isInstanceOf(EntityNotFoundException.class)
                );
    }

    @Test
    void testAddUser_success() throws Exception {
        String body = getSaveUserDtoJson();

        MvcResult mvcResult = mvc.perform(post("/api/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        )
                .andExpect(status().isCreated())
                .andReturn();

        UserDto userDto = parseResponse(mvcResult, UserDto.class);
        long userId = userDto.getId();
        assertThat(userId).isGreaterThanOrEqualTo(1);

        User user = userRepository.findById(userId).get();
        assertThat(user.getName()).isEqualTo("Ivanko");
        assertThat(user.getEmail()).isEqualTo("ivanko@test.com");
        assertThat(user.getPassword()).isEqualTo("12345678");
    }

    @Test
    void testAddUser_validation() throws Exception {
        mvc.perform(post("/api/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testAddUserWithDuplicateEmail() throws Exception {
        String body = getSaveUserDtoJson();

        mvc.perform(post("/api/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        String duplicateBody = """
          {
              "name": "Ivanko M",
              "email": "ivanko@test.com",
              "password": "12345678"
          }               
        """;

        mvc.perform(post("/api/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(duplicateBody))
                .andExpect(status().isConflict())
                .andExpect(result ->
                        assertThat(result.getResolvedException())
                                .isInstanceOf(DuplicateEmailException.class)
                );
    }

    @Test
    void testUpdateUser_success() throws Exception {
        User user = saveUserInTestDb();
        String body = getSaveUserDtoJson();

        MvcResult mvcResult = mvc.perform(put("/api/user/{id}", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();

        UserDto updatedUser = parseResponse(mvcResult, UserDto.class);
        assertThat(updatedUser.getName()).isEqualTo("Ivanko");
        assertThat(updatedUser.getEmail()).isEqualTo("ivanko@test.com");
    }


    @Test
    void testUpdateUser_notFound() throws Exception {
        String body = getSaveUserDtoJson();

        mvc.perform(put("/api/user/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(result ->
                        assertThat(result.getResolvedException())
                                .isInstanceOf(EntityNotFoundException.class)
                );
    }

    @Test
    void testUpdateUser_validation_anotherUserWithSameEmail() throws Exception {
        User user = saveUserInTestDb();

        User user2 = new User();
        user2.setName("Ivanko");
        user2.setEmail("ivanko@test.com");
        user2.setPassword("12345678");
        User savedUser = userRepository.save(user2);

        String body = """
                    {
                        "name": "Marusia 2",
                        "email": "ivanko@test.com",
                        "password": "12345678"
                    }  
                """;

        mvc.perform(put("/api/user/{id}", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(result ->
                        assertThat(result.getResolvedException())
                                .isInstanceOf(DuplicateEmailException.class)
                );
    }

    @Test
    void testUpdateUser_validation_sameUserWithSameEmail() throws Exception {
        User user = saveUserInTestDb();

        User user2 = new User();
        user2.setName("Ivanko");
        user2.setEmail("ivanko@test.com");
        user2.setPassword("12345678");
        User updatetUser = userRepository.save(user2);

        String body = """
                    {
                        "name": "Ivanko 2",
                        "email": "ivanko@test.com",
                        "password": "12345678"
                    }  
                """;

        mvc.perform(put("/api/user/{id}", updatetUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ivanko 2"))
                .andExpect(jsonPath("$.email").value("ivanko@test.com"));

        User updatedUser = userRepository.findById(updatetUser.getId()).get();
        assertThat(updatedUser.getName()).isEqualTo("Ivanko 2");
        assertThat(updatedUser.getEmail()).isEqualTo("ivanko@test.com");
        assertThat(updatedUser.getPassword()).isEqualTo("12345678");

    }

    @Test
    void testDeleteUser() throws Exception {
        User user = saveUserInTestDb();

        mvc.perform(delete("/api/user/{id}", user.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        assertThat(userRepository.existsById(user.getId())).isFalse();
    }

    @Test
    void testDeleteUser_notFound() throws Exception {
        mvc.perform(delete("/api/user/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(result ->
                        assertThat(result.getResolvedException())
                                .isInstanceOf(EntityNotFoundException.class)
                );
    }

    private <T>T parseResponse(MvcResult mvcResult, Class<T> c) {
        try {
            return objectMapper.readValue(mvcResult.getResponse().getContentAsString(), c);
        } catch (JsonProcessingException | UnsupportedEncodingException e) {
            throw new RuntimeException("Error parsing json", e);
        }
    }

    private String getSaveUserDtoJson(){
        String name = "Ivanko";
        String email = "ivanko@test.com";
        String password = "12345678";

        return String.format("{\"name\":\"%s\",\"email\":\"%s\",\"password\":\"%s\"}", name, email, password);
    }

    private User saveUserInTestDb(){
        User user = new User();
        user.setName("Marusia");
        user.setEmail("marusia@test.com");
        user.setPassword("12345678");
        return userRepository.save(user);
    }
}