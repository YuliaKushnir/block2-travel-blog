package org.example.block2travelblog.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.jackson.Jacksonized;

/**
 * DTO for resulting response after uploading data from json file
 */
@Getter
@Builder
@Jacksonized
@RequiredArgsConstructor
public class UploadedPostsRestResponse {

    private final String successMessage;
    private final String errorMessage;

}
