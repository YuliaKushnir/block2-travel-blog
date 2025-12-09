package org.example.block2travelblog.repository.specification;

import org.example.block2travelblog.data.Post;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;


public class PostSpecifications {
    public static Specification<Post> hasCountry(String country) {
        return (root, query, criteriaBuilder) ->
                country == null ? null : criteriaBuilder.equal(root.get("country"), country);
    }

    public static Specification<Post> hasCategory(String category) {
        return (root, query, criteriaBuilder) ->
                category == null ? null : criteriaBuilder.isMember(category, root.get("categories"));
    }

    public static Specification<Post> isCreatedAfter(LocalDate createdAfter) {
        return (root, query, criteriaBuilder) ->
                createdAfter == null ? null : criteriaBuilder.greaterThan(root.get("createdAt"), createdAfter);
    }

    public static Specification<Post> hasMinRating(Double minRating){
        return (root, query, criteriaBuilder) ->
                minRating == null ? null : criteriaBuilder.greaterThanOrEqualTo(root.get("rating"), minRating);

    }

    public static Specification<Post> hasUserId(Long userId) {
        return (root, query, criteriaBuilder) ->
                userId == null ? null : criteriaBuilder.equal(root.get("user").get("id"), userId);
    }

}
