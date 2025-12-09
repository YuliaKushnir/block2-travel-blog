package org.example.block2travelblog.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * DTO for post fields for filtering.
 */
@Getter
@Setter
public class PostFilteredFieldsDto implements PostFilter {

    private String country;
    private String category;
    private LocalDate createdAfter;
    private Double minRating;
    private Long userId;

}
