package org.example.block2travelblog.dto;

import java.time.LocalDate;

public interface PostFilter {

    String getCountry();
    String getCategory();
    LocalDate getCreatedAfter();
    Double getMinRating();
    Long getUserId();

}
