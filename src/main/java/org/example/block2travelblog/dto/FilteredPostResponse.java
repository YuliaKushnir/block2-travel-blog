package org.example.block2travelblog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * DTO for response with list of posts, number of pages and number of filtered elements (posts)
 */
@Data
@AllArgsConstructor
public class FilteredPostResponse {

    private List<PostDto> list;
    private int totalPages;
    private long totalElements;

}
