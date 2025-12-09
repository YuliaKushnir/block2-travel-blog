package org.example.block2travelblog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO for post data with user data.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExtendedPostDto {

    private Long id;
    private String title;
    private String content;
    private String country;
    private List<String> category;
    private LocalDate createdAt;
    private Double rating;
    private UserDto author;

}
