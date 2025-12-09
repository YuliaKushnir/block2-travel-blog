package org.example.block2travelblog.data;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Post persistent entity.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
        name = "posts",
        indexes = {
                @Index(name = "idx_post_country", columnList = "country"),
                @Index(name = "idx_post_created_at", columnList = "created_at"),
                @Index(name = "idx_post_rating", columnList = "rating"),
                @Index(name = "idx_post_user_id", columnList = "user_id")
        }
)
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String content;
    private String country;

    @ElementCollection
    private List<String> categories;

    private LocalDate createdAt;
    private Double rating;

    @ManyToOne
    @JoinColumn(name="user_id", nullable = false)
    private User user;

}
