package com.itt.newsaggregator.service;

import com.itt.newsaggregator.dto.SavedArticleRequestDTO;
import com.itt.newsaggregator.dto.SavedArticleResponseDTO;
import com.itt.newsaggregator.entities.Article;
import com.itt.newsaggregator.entities.SavedArticle;
import com.itt.newsaggregator.entities.User;
import com.itt.newsaggregator.repository.ArticleRepository;
import com.itt.newsaggregator.repository.SavedArticleRepository;
import com.itt.newsaggregator.repository.UserRepository;
import com.itt.newsaggregator.exception.UserNotFoundException;
import com.itt.newsaggregator.exception.ArticleNotFoundException;
import com.itt.newsaggregator.exception.InvalidInputException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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
                .orElseThrow(() -> new UserNotFoundException("User with ID " + dto.getUserId() + " not found"));
        Article article = articleRepository.findById(dto.getArticleId())
                .orElseThrow(() -> new ArticleNotFoundException("Article with ID " + dto.getArticleId() + " not found"));
        if (savedArticleRepository.existsByUserAndArticle(user, article)) {
            throw new InvalidInputException("Article already saved by user");
        }

        SavedArticle savedArticle = new SavedArticle();
        savedArticle.setUser(user);
        savedArticle.setArticle(article);
        savedArticle.setSavedAt(LocalDateTime.now());

        savedArticleRepository.save(savedArticle);
    }
    public List<SavedArticleResponseDTO> getSavedArticlesByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + userId + " not found"));

        List<SavedArticle> savedArticles = savedArticleRepository.findByUserOrderBySavedAtDesc(user);

        return savedArticles.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private SavedArticleResponseDTO convertToDTO(SavedArticle savedArticle) {
        Article article = savedArticle.getArticle();

        return new SavedArticleResponseDTO(
                savedArticle.getSavedArticleId(),
                article.getId(),
                article.getTitle(),
                article.getDescription(),
                article.getUrl(),
                article.getContent(),
                article.getPublishedAt() != null ? article.getPublishedAt().toString() : null,
                savedArticle.getSavedAt()
        );
    }

    public void deleteSavedArticle(Long savedArticleId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + userId + " not found"));

        SavedArticle savedArticle = savedArticleRepository.findBySavedArticleIdAndUser(savedArticleId, user)
                .orElseThrow(() -> new InvalidInputException("Saved article not found or you don't have permission to delete it"));

        savedArticleRepository.delete(savedArticle);
    }
}