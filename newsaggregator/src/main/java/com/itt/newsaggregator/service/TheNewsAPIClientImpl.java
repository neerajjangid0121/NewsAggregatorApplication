package com.itt.newsaggregator.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.itt.newsaggregator.dto.ArticleDTO;
import com.itt.newsaggregator.entities.Article;
import com.itt.newsaggregator.entities.CategoryArticleMapping;
import com.itt.newsaggregator.entities.ExternalAPIDetails;
import com.itt.newsaggregator.interfaces.NewsAPIClient;
import com.itt.newsaggregator.repository.ArticleRepository;
import com.itt.newsaggregator.repository.CategoryArticleMappingRepository;
import com.itt.newsaggregator.repository.CategoryRepository;
import com.itt.newsaggregator.repository.ExternalAPIRepository;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.*;

@Service
public class TheNewsAPIClientImpl implements NewsAPIClient {

    private final RestTemplate restTemplate;
    private final ExternalAPIRepository externalAPIRepository;
    private final ArticleRepository articleRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryArticleMappingRepository categoryArticleMappingRepository;
    private final NotificationService notificationService;

    public TheNewsAPIClientImpl(RestTemplate restTemplate,
                                ExternalAPIRepository externalAPIRepository,
                                ArticleRepository articleRepository,
                                CategoryRepository categoryRepository,
                                CategoryArticleMappingRepository categoryArticleMappingRepository,
                                NotificationService notificationService) {
        this.restTemplate = restTemplate;
        this.externalAPIRepository = externalAPIRepository;
        this.articleRepository = articleRepository;
        this.categoryRepository = categoryRepository;
        this.categoryArticleMappingRepository = categoryArticleMappingRepository;
        this.notificationService = notificationService;
    }

    @Override
    public List<Article> fetchNews() {
        ExternalAPIDetails api = getAPIConfig("TheNewsAPI");

        List<ArticleDTO> articleDTOs = fetchArticlesFromAPI(api.getUrl(), api.getApiKey());

        List<Article> parsedArticles = new ArrayList<>();

        for (ArticleDTO dto : articleDTOs) {
            if (articleRepository.existsByUrl(dto.getUrl())) continue;

            Article article = mapToEntity(dto, api);
            article = articleRepository.save(article);

            boolean mapped = mapCategories(article, dto.getCategories());
            if (!mapped) {
                mapToAllCategory(article);
            }

            System.out.println("🎯 Attempting to create notifications for article: " + article.getTitle());
            try {
                notificationService.createNotificationForArticle(article);
                System.out.println("✅ Successfully called createNotificationForArticle for: " + article.getTitle());
            } catch (Exception e) {
                System.err.println("❌ Failed to create notifications for article: " + e.getMessage());
                e.printStackTrace();
            }

            parsedArticles.add(article);
        }

        api.setLastFetched(LocalDateTime.now());
        externalAPIRepository.save(api);

        return parsedArticles;
    }

    private ExternalAPIDetails getAPIConfig(String serverName) {
        return externalAPIRepository.findByServerName(serverName)
                .orElseThrow(() -> new IllegalStateException("No ExternalAPIDetails for " + serverName));
    }

    private List<ArticleDTO> fetchArticlesFromAPI(String baseUrl, String apiKey) {
        URI uri = UriComponentsBuilder.fromUriString(baseUrl)
                .queryParam("api_token", apiKey)
                .build()
                .encode()
                .toUri();

        System.out.println("Fetching from URI: " + uri);

        ResponseEntity<JsonNode> response = restTemplate.exchange(
                uri, HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), JsonNode.class
        );

        JsonNode dataArray = response.getBody().path("data");
        if (!dataArray.isArray()) return Collections.emptyList();

        List<ArticleDTO> articles = new ArrayList<>();
        for (JsonNode node : dataArray) {
            articles.add(ArticleDTO.fromJson(node));
        }

        return articles;
    }

    private Article mapToEntity(ArticleDTO dto, ExternalAPIDetails apiDetails) {
        Article article = new Article();
        article.setTitle(dto.getTitle());
        article.setDescription(dto.getDescription());
        article.setContent(dto.getContent());
        article.setUrl(dto.getUrl());

        try {
            OffsetDateTime odt = OffsetDateTime.parse(dto.getPublishedAt());
            article.setPublishedAt(odt.toLocalDateTime());
        } catch (Exception e) {
            System.err.println("Error parsing publishedAt: " + dto.getPublishedAt());
            article.setPublishedAt(LocalDateTime.now());
        }

        article.setExternalAPIDetails(apiDetails);
        return article;
    }

    private boolean mapCategories(Article article, List<String> categoryNames) {
        boolean mapped = false;
        for (String categoryName : categoryNames) {
            categoryRepository.findByNameIgnoreCase(categoryName).ifPresentOrElse(
                    category -> {
                        CategoryArticleMapping mapping = new CategoryArticleMapping();
                        mapping.setArticle(article);
                        mapping.setCategory(category);
                        categoryArticleMappingRepository.save(mapping);
                        System.out.println("Mapped: " + article.getTitle() + " → " + categoryName);
                    },
                    () -> System.out.println("Not found: " + categoryName)
            );
            mapped = mapped || categoryRepository.findByNameIgnoreCase(categoryName).isPresent();
        }
        return mapped;
    }

    private void mapToAllCategory(Article article) {
        categoryRepository.findByNameIgnoreCase("all").ifPresent(allCategory -> {
            CategoryArticleMapping mapping = new CategoryArticleMapping();
            mapping.setArticle(article);
            mapping.setCategory(allCategory);
            categoryArticleMappingRepository.save(mapping);
            System.out.println("Mapped to 'ALL': " + article.getTitle());
        });
    }
}