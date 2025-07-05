package com.itt.newsaggregator.controller;

import com.itt.newsaggregator.dto.ArticleDTO;
import com.itt.newsaggregator.dto.SavedArticleRequestDTO;
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

    @PostMapping("/saved")
    public ResponseEntity<Void> saveArticle(@RequestBody SavedArticleRequestDTO requestDTO) {
        savedArticleService.saveArticle(requestDTO);
        return ResponseEntity.ok().build();
    }
}
