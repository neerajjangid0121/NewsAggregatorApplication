package com.itt.newsaggregator.entities;

import java.time.LocalDateTime;

import com.itt.newsaggregator.Enums.ArticleStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Articles")
public class Article {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(nullable = false, unique = true)
    private String url;

    @Column(nullable = false,length = 2000)
    private String description;

    @Column(nullable = false)
    private String content;

    @ManyToOne
    @JoinColumn(name = "server_id", nullable = false)
    private ExternalAPIDetails externalAPIDetails;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ArticleStatus status = ArticleStatus.PUBLIC;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hidden_by")
    private User hiddenBy;

    @Column(name = "hidden_at")
    private LocalDateTime hiddenAt;

    @Column(name = "report_count", nullable = false)
    private Integer reportCount = 0;

    @Column(name = "last_reported_at")
    private LocalDateTime lastReportedAt;

    @Column(name = "like_count", nullable = false)
    private int likeCount = 0;

    @Column(name = "dislike_count", nullable = false)
    private int dislikeCount = 0;
}
