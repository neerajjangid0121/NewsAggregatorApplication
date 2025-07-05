package com.itt.newsaggregator.service;

import com.itt.newsaggregator.dto.SavedArticleRequestDTO;
import com.itt.newsaggregator.entities.Article;
import com.itt.newsaggregator.entities.SavedArticle;
import com.itt.newsaggregator.entities.User;
import com.itt.newsaggregator.repository.ArticleRepository;
import com.itt.newsaggregator.repository.SavedArticleRepository;
import com.itt.newsaggregator.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SavedArticleService {

    private final SavedArticleRepository savedArticleRepository;
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;

    public SavedArticleService(SavedArticleRepository savedArticleRepository,
                               ArticleRepository articleRepository,
                               UserRepository userRepository) {
        this.savedArticleRepository = savedArticleRepository;
        this.articleRepository = articleRepository;
        this.userRepository = userRepository;
    }

    public void saveArticle(SavedArticleRequestDTO dto) {
        System.out.println("Incoming DTO => userId: " + dto.getUserId() + ", articleId: " + dto.getArticleId());
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Article article = articleRepository.findById(dto.getArticleId())
                .orElseThrow(() -> new RuntimeException("Article not found"));

        if (savedArticleRepository.existsByUserAndArticle(user, article)) {
            throw new RuntimeException("Already saved");
        }

        SavedArticle savedArticle = new SavedArticle();
        savedArticle.setUser(user);
        savedArticle.setArticle(article);
        savedArticle.setSavedAt(LocalDateTime.now());

        savedArticleRepository.save(savedArticle);
    }
}