package org.example.block2travelblog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for post data for saving into database
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SavePostDto {


    @NotBlank(message = "title is required")
    @Size(max = 255, message = "title must be less than 255 characters")
    private String title;

    @NotBlank(message = "content is required")
    @Size(min = 10, message = "content must be at least 10 characters")
    private String content;

    @NotBlank(message = "country is required")
    private String country;

    @NotNull(message = "categories are required")
    @Size(min = 1, message = "at least one categories is required")
    private List<String> categories;

    @NotNull(message = "userId is required")
    private Long userId;

}
