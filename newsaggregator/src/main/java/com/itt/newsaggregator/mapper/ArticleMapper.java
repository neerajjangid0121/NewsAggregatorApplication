package com.itt.newsaggregator.mapper;

import com.itt.newsaggregator.dto.ArticleDTO;
import com.itt.newsaggregator.entities.Article;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class ArticleMapper {

    public ArticleDTO toDto(Article article) {
        return new ArticleDTO(
                article.getId(),
                article.getTitle(),
                article.getPublishedAt().toString(),
                article.getUrl(),
                article.getDescription(),
                article.getContent(),
                article.getExternalAPIDetails().getServerName(),
                new ArrayList<>(),
                article.getReportCount(),
                article.getStatus() != null ? article.getStatus().name() : null,
                article.getLikeCount(),
                article.getDislikeCount()
        );
    }
}