package com.itt.newsaggregator.service;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.itt.newsaggregator.entities.Article;
import com.itt.newsaggregator.entities.Category;
import com.itt.newsaggregator.entities.CategoryArticleMapping;
import com.itt.newsaggregator.entities.ExternalAPIDetails;
import com.itt.newsaggregator.interfaces.NewsAPIClient;
import com.itt.newsaggregator.repository.*;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class NewsAPIClientImpl implements NewsAPIClient {

    private final RestTemplate restTemplate;
    private final ExternalAPIRepository externalAPIRepository;
    private final ArticleRepository articleRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryArticleMappingRepository categoryArticleMappingRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    public NewsAPIClientImpl(RestTemplate restTemplate,
                             ExternalAPIRepository externalAPIRepository,
                             ArticleRepository articleRepository,
                             CategoryRepository categoryRepository,
                             CategoryArticleMappingRepository categoryArticleMappingRepository) {
        this.restTemplate = restTemplate;
        this.externalAPIRepository = externalAPIRepository;
        this.articleRepository = articleRepository;
        this.categoryRepository = categoryRepository;
        this.categoryArticleMappingRepository = categoryArticleMappingRepository;
    }

    @Override
    public List<Article> fetchNews() {
        ExternalAPIDetails api = getAPIConfig("NewsAPI");
        JsonNode articlesNode = fetchArticlesFromAPI(api);
        if (articlesNode == null || !articlesNode.isArray()) return Collections.emptyList();

        List<Article> savedArticles = new ArrayList<>();

        for (JsonNode node : articlesNode) {
            Optional<Article> articleOpt = parseAndSaveArticle(node, api);
            articleOpt.ifPresent(article -> {
                mapArticleToCategory(article);
                savedArticles.add(article);
            });
        }

        updateLastFetched(api);
        return savedArticles;
    }

    private ExternalAPIDetails getAPIConfig(String serverName) {
        return externalAPIRepository.findByServerName(serverName)
                .orElseThrow(() -> new IllegalStateException("No ExternalAPIDetails for " + serverName));
    }

    private JsonNode fetchArticlesFromAPI(ExternalAPIDetails api) {
        URI uri = UriComponentsBuilder.fromUriString(api.getUrl())
                .queryParam("apiKey", api.getApiKey())
                .build().encode().toUri();

        System.out.println("Fetching from URI: " + uri);
        try {
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    uri, HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), JsonNode.class);
            return response.getBody().path("articles");
        } catch (Exception e) {
            System.out.println("Error fetching from API: " + e.getMessage());
            return null;
        }
    }

    private Optional<Article> parseAndSaveArticle(JsonNode node, ExternalAPIDetails api) {
        String rawUrl = normalizeUrl(node.path("url").asText());
        if (rawUrl == null || articleRepository.existsByUrl(rawUrl)) {
            System.out.println("Skipped duplicate or invalid URL: " + rawUrl);
            return Optional.empty();
        }

        Article article = new Article();
        article.setTitle(node.path("title").asText(""));
        article.setDescription(node.path("description").asText(""));
        article.setContent(node.path("content").asText(""));
        article.setUrl(rawUrl);
        article.setExternalAPIDetails(api);

        try {
            article.setPublishedAt(LocalDateTime.parse(node.path("publishedAt").asText(), DATE_FORMATTER));
        } catch (Exception e) {
            article.setPublishedAt(LocalDateTime.now());
        }

        return Optional.of(articleRepository.save(article));
    }

    private String normalizeUrl(String url) {
        if (url == null || url.isBlank()) return null;
        try {
            URI uri = URI.create(url.trim().toLowerCase());
            return uri.getScheme() + "://" + uri.getHost() + uri.getPath();
        } catch (Exception e) {
            System.out.println("Invalid URL skipped: " + url);
            return null;
        }
    }

    private void mapArticleToCategory(Article article) {
        String fullText = (article.getTitle() + " " + article.getDescription() + " " + article.getContent()).toLowerCase();
        Map<Category, Integer> matchCounts = new HashMap<>();

        for (Category category : categoryRepository.findAll()) {
            if (category.getKeywords() == null) continue;

            int matches = countKeywordMatches(fullText, category.getKeywords());
            if (matches > 0) {
                matchCounts.put(category, matches);
                System.out.println("Matched " + matches + " keyword(s) for category: " + category.getName());
            }
        }

        Category selected = matchCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElseGet(() -> categoryRepository.findByNameIgnoreCase("all").orElse(null));

        if (selected != null) {
            CategoryArticleMapping mapping = new CategoryArticleMapping();
            mapping.setArticle(article);
            mapping.setCategory(selected);
            categoryArticleMappingRepository.save(mapping);
            System.out.println("Mapped to: " + selected.getName());
        } else {
            System.out.println("No category found and 'all' fallback missing.");
        }
    }

    private int countKeywordMatches(String text, String keywordsCsv) {
        int count = 0;
        for (String keyword : keywordsCsv.split(",")) {
            if (text.contains(keyword.trim().toLowerCase())) {
                count++;
            }
        }
        return count;
    }

    private void updateLastFetched(ExternalAPIDetails api) {
        api.setLastFetched(LocalDateTime.now());
        externalAPIRepository.save(api);
    }
}