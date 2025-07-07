package com.itt.newsaggregator.controller;

import com.itt.newsaggregator.dto.ArticleDTO;
import com.itt.newsaggregator.dto.SavedArticleRequestDTO;
import com.itt.newsaggregator.dto.SavedArticleResponseDTO;
import com.itt.newsaggregator.service.ArticleService;
import com.itt.newsaggregator.service.SavedArticleService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserNewsController {
    private final ArticleService articleService;
    private final SavedArticleService savedArticleService;

    public UserNewsController(ArticleService articleService, SavedArticleService savedArticleService) {
        this.articleService = articleService;
        this.savedArticleService = savedArticleService;
    }

    @GetMapping("/headlines")
    public ResponseEntity<List<ArticleDTO>> getHeadlines(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "category", required = false) String category
    ) {
        return ResponseEntity.ok(articleService.getArticlesBetweenDates(startDate, endDate, category));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ArticleDTO>> searchArticles(
            @RequestParam("query") String query,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "sortBy", required = false) String sortBy
    ) {
        return ResponseEntity.ok(articleService.searchArticles(query, startDate, endDate, sortBy));
    }

    @PostMapping("/saved")
    public ResponseEntity<Void> saveArticle(@RequestBody SavedArticleRequestDTO requestDTO) {
        savedArticleService.saveArticle(requestDTO);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/saved/{userId}")
    public ResponseEntity<List<SavedArticleResponseDTO>> getSavedArticles(@PathVariable Long userId) {
        List<SavedArticleResponseDTO> savedArticles = savedArticleService.getSavedArticlesByUserId(userId);
        return ResponseEntity.ok(savedArticles);
    }

    @DeleteMapping("/saved/{savedArticleId}/user/{userId}")
    public ResponseEntity<Void> deleteSavedArticle(@PathVariable Long savedArticleId, @PathVariable Long userId) {
        savedArticleService.deleteSavedArticle(savedArticleId, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/articles/{articleId}/report")
    public ResponseEntity<Void> reportArticle(
            @PathVariable Long articleId,
            @RequestParam Long userId,
            @RequestParam String reason) {
        articleService.reportArticle(articleId, userId, reason);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/articles/{articleId}/visibility")
    public ResponseEntity<Void> toggleArticleVisibility(
            @PathVariable Long articleId,
            @RequestParam Long adminUserId,
            @RequestParam boolean hide,
            @RequestParam(required = false) String reason) {
        articleService.toggleArticleVisibility(articleId, adminUserId, hide, reason);
        return ResponseEntity.ok().build();
    }
}
