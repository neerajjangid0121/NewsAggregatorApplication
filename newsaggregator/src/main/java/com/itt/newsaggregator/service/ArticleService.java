package com.itt.newsaggregator.service;

import com.itt.newsaggregator.dto.ArticleDTO;
import com.itt.newsaggregator.entities.Article;
import com.itt.newsaggregator.Enums.ArticleStatus;
import com.itt.newsaggregator.entities.CategoryArticleMapping;
import com.itt.newsaggregator.entities.User;
import com.itt.newsaggregator.mapper.ArticleMapper;
import com.itt.newsaggregator.repository.ArticleRepository;
import com.itt.newsaggregator.repository.CategoryArticleMappingRepository;
import com.itt.newsaggregator.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ArticleService {

    private final CategoryArticleMappingRepository mappingRepo;
    private final ArticleMapper articleMapper;
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final CategoryService categoryService;
    private final KeywordService keywordService;

    @Value("${moderation.auto-hide-threshold:5}")
    private int autoHideThreshold;

    public ArticleService(CategoryArticleMappingRepository mappingRepo,
                          ArticleMapper articleMapper,
                          ArticleRepository articleRepository,
                          UserRepository userRepository,
                          CategoryService categoryService,
                          KeywordService keywordService) {
        this.mappingRepo = mappingRepo;
        this.articleMapper = articleMapper;
        this.articleRepository = articleRepository;
        this.userRepository = userRepository;
        this.categoryService = categoryService;
        this.keywordService = keywordService;
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

            // Skip articles that should be filtered
            if (shouldFilterArticle(article)) {
                continue;
            }

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
            // Skip articles that should be filtered
            if (shouldFilterArticle(article)) {
                continue;
            }

            Long articleId = article.getId();
            articleMap.putIfAbsent(articleId, articleMapper.toDto(article));
        }

        // Add categories for each article
        for (Article article : articles) {
            // Skip articles that should be filtered
            if (shouldFilterArticle(article)) {
                continue;
            }

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

    // Article Moderation Methods

    @Transactional
    public void reportArticle(Long articleId, Long userId, String reason) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("Article not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update article report count
        article.setReportCount(article.getReportCount() + 1);
        article.setLastReportedAt(LocalDateTime.now());

        // Auto-hide if threshold reached
        if (article.getReportCount() >= autoHideThreshold) {
            article.setStatus(ArticleStatus.HIDDEN);
            article.setHiddenAt(LocalDateTime.now());
            System.out.println("🚨 Article auto-hidden due to high report count: " + article.getTitle());
        }

        articleRepository.save(article);
    }

    @Transactional
    public void toggleArticleVisibility(Long articleId, Long adminUserId, boolean hide, String reason) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("Article not found"));

        User admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        if (hide) {
            article.setStatus(ArticleStatus.HIDDEN);
            article.setHiddenBy(admin);
            article.setHiddenAt(LocalDateTime.now());
        } else {
            article.setStatus(ArticleStatus.PUBLIC);
            article.setHiddenBy(null);
            article.setHiddenAt(null);
        }

        articleRepository.save(article);
    }

    public List<Article> getArticlesWithHighReportCount() {
        return articleRepository.findArticlesWithHighReportCount(autoHideThreshold);
    }

    public Article getArticleById(Long articleId) {
        return articleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("Article not found"));
    }

    // Filter articles based on restrictions
    public boolean shouldFilterArticle(Article article) {
        // Check if article is hidden
        if (article.getStatus() != ArticleStatus.PUBLIC) {
            return true;
        }

        // Check category restrictions
        List<CategoryArticleMapping> categoryMappings = mappingRepo.findByArticle(article);
        for (CategoryArticleMapping mapping : categoryMappings) {
            if (categoryService.isCategoryRestricted(mapping.getCategory().getCategoryId())) {
                return true;
            }
        }

        // Check keyword restrictions
        if (keywordService.hasRestrictedKeywords(article.getTitle(), article.getDescription(), article.getContent())) {
            return true;
        }

        return false;
    }

    public List<ArticleDTO> getArticlesWithReports() {
        List<Article> articles = articleRepository.findByReportCountGreaterThan(0);
        return articles.stream()
                .map(articleMapper::toDto)
                .collect(Collectors.toList());
    }
}
