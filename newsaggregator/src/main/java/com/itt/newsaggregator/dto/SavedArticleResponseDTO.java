package com.itt.newsaggregator.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SavedArticleResponseDTO {
    private Long savedArticleId;
    private Long articleId;
    private String title;
    private String description;
    private String url;
    private String content;
    private String publishedAt;
    private LocalDateTime savedAt;
}