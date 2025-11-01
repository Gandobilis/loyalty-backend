package com.multi.loyaltybackend.faq.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * FAQ entity representing frequently asked questions
 */
@Entity
@Table(name = "faqs", indexes = {
        @Index(name = "idx_category", columnList = "category"),
        @Index(name = "idx_publish", columnList = "publish"),
        @Index(name = "idx_popular", columnList = "popular")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FAQ {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Category is required")
    @Size(max = 100, message = "Category must not exceed 100 characters")
    @Column(nullable = false, length = 100)
    private String category;

    @NotBlank(message = "Question is required")
    @Size(max = 500, message = "Question must not exceed 500 characters")
    @Column(nullable = false, length = 500)
    private String question;

    @NotBlank(message = "Answer is required")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String answer;

    @NotNull(message = "Publish status is required")
    @Column(nullable = false)
    @Builder.Default
    private Boolean publish = false;

    @NotNull(message = "Popular status is required")
    @Column(nullable = false)
    @Builder.Default
    private Boolean popular = false;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (publish == null) {
            publish = false;
        }
        if (popular == null) {
            popular = false;
        }
    }
}
