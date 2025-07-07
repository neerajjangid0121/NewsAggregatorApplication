package com.itt.newsaggregator.service;

import com.itt.newsaggregator.entities.*;
import com.itt.newsaggregator.Enums.ReactionType;
import com.itt.newsaggregator.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ArticleRepository articleRepository;
    @Autowired
    private UserKeywordMappingRepository userKeywordMappingRepository;
    @Autowired
    private ArticleReactionRepository articleReactionRepository;
    @Autowired
    private SavedArticleRepository savedArticleRepository;
    @Autowired
    private UserArticleReadHistoryService userArticleReadHistoryService;

    public List<Article> getRecommendationsForUser(Long userId, int limit) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) return Collections.emptyList();
        User user = userOpt.get();

        // 1. Get user keywords
        List<UserKeywordMapping> keywordMappings = userKeywordMappingRepository.findByUserAndIsEnable(user, true);
        Set<String> userKeywords = keywordMappings.stream()
                .map(UserKeywordMapping::getKeyword)
                .filter(Objects::nonNull)
                .map(Keyword::getKeyword)
                .collect(Collectors.toSet());

        // 2. Get liked articles
        List<Article> likedArticles = articleReactionRepository.findAll().stream()
                .filter(r -> r.getUser().equals(user) && r.getReactionType() == ReactionType.LIKE)
                .map(ArticleReaction::getArticle)
                .collect(Collectors.toList());

        // 3. Get saved articles
        List<SavedArticle> savedArticles = savedArticleRepository.findByUserOrderBySavedAtDesc(user);
        Set<Article> savedArticleSet = savedArticles.stream().map(SavedArticle::getArticle).collect(Collectors.toSet());

        // 4. Get read articles
        List<UserArticleReadHistory> readHistory = userArticleReadHistoryService.getReadHistoryForUser(user);
        Set<Article> readArticles = readHistory.stream().map(UserArticleReadHistory::getArticle).collect(Collectors.toSet());

        // 5. Score all articles
        List<Article> allArticles = articleRepository.findAll();
        Map<Article, Integer> articleScores = new HashMap<>();
        for (Article article : allArticles) {
            int score = 0;
            // Score for keyword match
            for (String keyword : userKeywords) {
                if ((article.getTitle() != null && article.getTitle().toLowerCase().contains(keyword.toLowerCase())) ||
                        (article.getContent() != null && article.getContent().toLowerCase().contains(keyword.toLowerCase()))) {
                    score += 5;
                }
            }
            // Score for similarity to liked/saved/read articles (category match)
            if (likedArticles.contains(article)) score += 10;
            if (savedArticleSet.contains(article)) score += 7;
            if (readArticles.contains(article)) score += 3;
            articleScores.put(article, score);
        }
        // Sort by score descending, then by recency
        List<Article> recommended = articleScores.entrySet().stream()
                .sorted((a, b) -> {
                    int cmp = Integer.compare(b.getValue(), a.getValue());
                    if (cmp == 0) {
                        // If scores are equal, prefer more recent
                        return b.getKey().getPublishedAt().compareTo(a.getKey().getPublishedAt());
                    }
                    return cmp;
                })
                .map(Map.Entry::getKey)
                .limit(limit)
                .collect(Collectors.toList());
        return recommended;
    }
}