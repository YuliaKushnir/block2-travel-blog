package org.example.block2travelblog.service;

import jakarta.servlet.http.HttpServletResponse;
import org.example.block2travelblog.dto.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * Service Interface for posts operations.
 */
public interface PostService {

    PostDto savePost(SavePostDto savePostDto);

    ExtendedPostDto getPostById(Long id);

    PostDto updatePost(Long id, SavePostDto savePostDto);

    void deletePost(Long id);

    FilteredPostResponse search(PostQueryDto postQueryDto);

    void generateReport(HttpServletResponse response, PostFilteredFieldsDto postFilteredFieldsDto);

    UploadedPostsRestResponse uploadFromFile(MultipartFile file);
}
