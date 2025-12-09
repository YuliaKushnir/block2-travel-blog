package org.example.block2travelblog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO for post data with user id.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostDto {

    private Long id;
    private String title;
    private String content;
    private String country;
    private List<String> categories;
    private LocalDate createdAt;
    private Double rating;
    private Long userId;
}
