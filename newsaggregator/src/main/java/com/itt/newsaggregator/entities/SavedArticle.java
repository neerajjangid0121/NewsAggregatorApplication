package com.itt.newsaggregator.entities;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "SavedArticles")
public class SavedArticle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column (name = "saved_article_id")
    private Long savedArticleId;

    @Column(name = "saved_at")
    private LocalDateTime savedAt;

    @ManyToOne
    @JoinColumn(name = "article_id", nullable = false)
    private Article articleId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User userId;
}