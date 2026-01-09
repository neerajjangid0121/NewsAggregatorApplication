package com.itt.newsaggregator.controller;

import com.itt.newsaggregator.dto.ArticleDTO;
import com.itt.newsaggregator.entities.Article;
import com.itt.newsaggregator.entities.User;
import com.itt.newsaggregator.service.RecommendationService;
import com.itt.newsaggregator.service.AuthService;
import com.itt.newsaggregator.mapper.ArticleMapper;
import com.itt.newsaggregator.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {
    @Autowired
    private RecommendationService recommendationService;
    @Autowired
    private ArticleMapper articleMapper;
    @Autowired
    private AuthService authService;
    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public List<ArticleDTO> getRecommendations(@AuthenticationPrincipal org.springframework.security.core.userdetails.User principal) {
        User user = userRepository.findByUsername(principal.getUsername()).orElseThrow();
        List<Article> recommended = recommendationService.getRecommendationsForUser(user.getId(), 20);
        return recommended.stream().map(articleMapper::toDto).collect(Collectors.toList());
    }
}