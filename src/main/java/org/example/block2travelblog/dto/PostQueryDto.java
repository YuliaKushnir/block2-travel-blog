package org.example.block2travelblog.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * DTO for requested post fields for filtering, page number and page size.
 */
@Getter
@Setter
public class PostQueryDto implements PostFilter {

    private String country;
    private String category;
    private LocalDate createdAfter;
    private Double minRating;
    private Long userId;

    @NotNull(message = "page number is required")
    private int from;

    @NotNull(message = "page size is required")
    private int size;

}
