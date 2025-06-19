package com.itt.newsaggregator.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="userId", nullable=false)
    private User user;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="categoryMappingId", nullable=false)
    private CategoryArticleMapping categoryMapping;

    @Column(nullable=false)
    private boolean isRead = false;

    @Column(nullable=false, updatable=false)
    private Instant createdAt;

    @Column(nullable=false)
    private Instant sendAt;

    @Column(nullable=false)
    private String message;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }
}
