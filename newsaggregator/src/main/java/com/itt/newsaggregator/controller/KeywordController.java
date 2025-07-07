package com.itt.newsaggregator.controller;

import com.itt.newsaggregator.service.KeywordService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/keywords")
public class KeywordController {
    private final KeywordService keywordService;

    public KeywordController(KeywordService keywordService) {
        this.keywordService = keywordService;
    }

    // Restrict keyword
    @PostMapping("/restrict")
    public ResponseEntity<Void> restrictKeyword(
            @RequestParam String keyword,
            @RequestParam Long adminUserId,
            @RequestParam String reason) {
        keywordService.restrictKeyword(keyword, adminUserId, reason);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/restrict")
    public ResponseEntity<Void> removeKeywordRestriction(@RequestParam String keyword) {
        keywordService.unrestrictKeyword(keyword);
        return ResponseEntity.ok().build();
    }

    @GetMapping("")
    public ResponseEntity<?> getAllKeywords() {
        return ResponseEntity.ok(keywordService.getAllKeywords());
    }

    @GetMapping("/restricted")
    public ResponseEntity<?> getRestrictedKeywords() {
        return ResponseEntity.ok(keywordService.getRestrictedKeywords());
    }
}