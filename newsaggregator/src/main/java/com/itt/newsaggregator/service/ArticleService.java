package com.itt.newsaggregator.service;

import com.itt.newsaggregator.dto.ArticleDTO;
import com.itt.newsaggregator.entities.Article;
import com.itt.newsaggregator.entities.CategoryArticleMapping;
import com.itt.newsaggregator.mapper.ArticleMapper;
import com.itt.newsaggregator.repository.ArticleRepository;
import com.itt.newsaggregator.repository.CategoryArticleMappingRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ArticleService {

    private final CategoryArticleMappingRepository mappingRepo;
    private final ArticleMapper articleMapper;
    private final ArticleRepository articleRepository;

    public ArticleService(CategoryArticleMappingRepository mappingRepo, ArticleMapper articleMapper,  ArticleRepository articleRepository) {
        this.mappingRepo = mappingRepo;
        this.articleMapper = articleMapper;
        this.articleRepository = articleRepository;
    }

    public List<ArticleDTO> getArticlesBetweenDates(LocalDate start, LocalDate end, String category) {
        LocalDateTime startTime = start.atStartOfDay();
        LocalDateTime endTime = end.plusDays(1).atStartOfDay();

        List<CategoryArticleMapping> mappings = (category == null || category.isBlank())
                ? mappingRepo.findByDateRange(startTime, endTime)
                : mappingRepo.findByDateRangeAndCategory(startTime, endTime, category);

        Map<Long, ArticleDTO> articleMap = new LinkedHashMap<>();

        for (CategoryArticleMapping cam : mappings) {
            Article article = cam.getArticle();
            Long articleId = article.getId();

            articleMap.putIfAbsent(articleId, articleMapper.toDto(article));
            ArticleDTO dto = articleMap.get(articleId);
            dto.getCategories().add(cam.getCategory().getName());
        }

        return new ArrayList<>(articleMap.values());
    }

    public List<ArticleDTO> searchArticles(String query, LocalDate startDate, LocalDate endDate, String sortBy) {
        List<Article> articles;

        if (startDate != null && endDate != null) {
            LocalDateTime startTime = startDate.atStartOfDay();
            LocalDateTime endTime = endDate.plusDays(1).atStartOfDay();
            articles = articleRepository.searchArticles(query, startTime, endTime);
        } else {
            articles = articleRepository.searchArticlesWithoutDateRange(query);
        }

        // Convert to DTOs and add categories
        Map<Long, ArticleDTO> articleMap = new LinkedHashMap<>();

        for (Article article : articles) {
            Long articleId = article.getId();
            articleMap.putIfAbsent(articleId, articleMapper.toDto(article));
        }

        // Add categories for each article
        for (Article article : articles) {
            List<CategoryArticleMapping> mappings = mappingRepo.findByArticle(article);
            ArticleDTO dto = articleMap.get(article.getId());
            for (CategoryArticleMapping cam : mappings) {
                dto.getCategories().add(cam.getCategory().getName());
            }
        }

        List<ArticleDTO> result = new ArrayList<>(articleMap.values());

        // Apply sorting if specified
        if (sortBy != null && !sortBy.isBlank()) {
            switch (sortBy.toLowerCase()) {
                case "likes":
                    // For now, sort by published date (likes/dislikes not implemented yet)
                    result.sort(Comparator.comparing(ArticleDTO::getPublishedAt).reversed());
                    break;
                case "dislikes":
                    // For now, sort by published date (likes/dislikes not implemented yet)
                    result.sort(Comparator.comparing(ArticleDTO::getPublishedAt));
                    break;
                default:
                    // Default sort by published date (newest first)
                    result.sort(Comparator.comparing(ArticleDTO::getPublishedAt).reversed());
            }
        } else {
            // Default sort by published date (newest first)
            result.sort(Comparator.comparing(ArticleDTO::getPublishedAt).reversed());
        }

        return result;
    }
}
