package com.itt.newsaggregator.service;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
public class NewsAPIClientImpl implements NewsAPIClient{
    private final RestTemplate restTemplate;
    private final ExternalAPIRepository externalAPIRepository;

    public NewsAPIClientImpl(RestTemplate restTemplate,ExternalAPIRepository externalAPIRepository) {
        this.restTemplate = restTemplate;
        this.externalAPIRepository  = externalAPIRepository;
    }

    @Override
    public List<Article> fetchNews() {
        ExternalAPIDetails api = externalAPIRepository.findByServerName("NewsAPI").orElseThrow(() -> new IllegalStateException("No ExternalAPIDetails for NewsAPI"));
        URI uri = UriComponentsBuilder
                .fromUriString(api.getUrl())
                .queryParam("apiKey", api.getApiKey())
                .build()
                .encode()
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Object> entity = new HttpEntity<>(null, headers);
        ResponseEntity<JsonNode> resp = restTemplate.exchange(uri, HttpMethod.GET,entity,JsonNode.class);


        JsonNode arr = resp.getBody().path("articles");
        if (!arr.isArray()) return Collections.emptyList();

        List<Article> parsedArticles = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ISO_DATE_TIME;

        for (JsonNode node : arr) {
            Article a = new Article();
            a.setTitle(node.path("title").asText(""));
            a.setDescription(node.path("description").asText(""));
            a.setContent(node.path("content").asText(""));
            a.setUrl(node.path("url").asText(""));
            a.setPublishedAt(
                    LocalDateTime.parse(node.path("publishedAt").asText(), fmt)
            );
            a.setExternalAPIDetails(api);
            parsedArticles.add(a);
        }

        api.setLastFetched(LocalDateTime.now());
        externalAPIRepository.save(api);

        return parsedArticles;
    }
}
