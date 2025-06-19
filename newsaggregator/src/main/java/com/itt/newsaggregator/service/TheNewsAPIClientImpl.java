package com.itt.newsaggregator.service;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.itt.newsaggregator.repository.ArticleRepository;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.itt.newsaggregator.entities.Article;
import com.itt.newsaggregator.entities.ExternalAPIDetails;
import com.itt.newsaggregator.interfaces.NewsAPIClient;
import com.itt.newsaggregator.repository.ExternalAPIRepository;

import org.springframework.http.HttpHeaders;

@Service
public class TheNewsAPIClientImpl implements NewsAPIClient {
    private final RestTemplate restTemplate;
    private final ExternalAPIRepository externalAPIRepository;
    private final ArticleRepository articleRepository;

    public TheNewsAPIClientImpl(RestTemplate restTemplate, ExternalAPIRepository externalAPIRepository,ArticleRepository articleRepository) {
        this.restTemplate = restTemplate;
        this.externalAPIRepository = externalAPIRepository;
        this.articleRepository = articleRepository;
    }

    @Override
    public List<Article> fetchNews() {
        ExternalAPIDetails api = externalAPIRepository.findByServerName("TheNewsAPI")
                .orElseThrow(() -> new IllegalStateException("No ExternalAPIDetails for TheNewsAPI"));

        URI uri = UriComponentsBuilder
                .fromUriString(api.getUrl())
                .queryParam("api_token", api.getApiKey())
                .build()
                .encode()
                .toUri();

        System.out.println("uri--------"+uri);
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Object> entity = new HttpEntity<>(null, headers);

        ResponseEntity<JsonNode> response = restTemplate.exchange(uri, HttpMethod.GET, entity, JsonNode.class);

        JsonNode dataArray = response.getBody().path("data");
        if (!dataArray.isArray()) return Collections.emptyList();

        List<Article> parsedArticles = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

        for (JsonNode node : dataArray) {
            String url = node.path("url").asText("");
            if (articleRepository.existsByUrl(url)) {
                continue;
            }

            Article article = new Article();
            article.setTitle(node.path("title").asText(""));
            article.setDescription(node.path("description").asText(""));
            article.setContent(node.path("content").asText(""));
            article.setUrl(url);

            try {
                article.setPublishedAt(LocalDateTime.parse(node.path("published_at").asText(""), formatter));
            } catch (Exception e) {
                article.setPublishedAt(LocalDateTime.now());
            }

            article.setExternalAPIDetails(api);
            parsedArticles.add(article);
        }

        api.setLastFetched(LocalDateTime.now());
        externalAPIRepository.save(api);

        return parsedArticles;
    }
}
