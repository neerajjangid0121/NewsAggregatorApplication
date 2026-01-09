package com.itt.newsaggregator.service;

import com.itt.newsaggregator.dto.SavedArticleRequestDTO;
import com.itt.newsaggregator.dto.SavedArticleResponseDTO;
import com.itt.newsaggregator.entities.Article;
import com.itt.newsaggregator.entities.SavedArticle;
import com.itt.newsaggregator.entities.User;
import com.itt.newsaggregator.exception.ArticleNotFoundException;
import com.itt.newsaggregator.exception.InvalidInputException;
import com.itt.newsaggregator.exception.UserNotFoundException;
import com.itt.newsaggregator.repository.ArticleRepository;
import com.itt.newsaggregator.repository.SavedArticleRepository;
import com.itt.newsaggregator.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import java.time.LocalDateTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

class SavedArticleServiceTest {
    @Mock private SavedArticleRepository savedArticleRepository;
    @Mock private ArticleRepository articleRepository;
    @Mock private UserRepository userRepository;
    private SavedArticleService savedArticleService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        savedArticleService = new SavedArticleService(savedArticleRepository, articleRepository, userRepository);
    }

    @Test
    @DisplayName("saveArticle saves if not already saved")
    void saveArticle_SavesIfNotExists() {
        SavedArticleRequestDTO dto = new SavedArticleRequestDTO(1L, 2L);
        User user = new User(); user.setId(1L);
        Article article = new Article(); article.setId(2L);
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Mockito.when(articleRepository.findById(2L)).thenReturn(Optional.of(article));
        Mockito.when(savedArticleRepository.existsByUserAndArticle(user, article)).thenReturn(false);
        Mockito.when(savedArticleRepository.save(any(SavedArticle.class))).thenAnswer(invocation -> invocation.getArgument(0));
        assertDoesNotThrow(() -> savedArticleService.saveArticle(dto));
    }

    @Test
    @DisplayName("saveArticle throws if already saved")
    void saveArticle_ThrowsIfAlreadySaved() {
        SavedArticleRequestDTO dto = new SavedArticleRequestDTO(1L, 2L);
        User user = new User(); user.setId(1L);
        Article article = new Article(); article.setId(2L);
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Mockito.when(articleRepository.findById(2L)).thenReturn(Optional.of(article));
        Mockito.when(savedArticleRepository.existsByUserAndArticle(user, article)).thenReturn(true);
        assertThrows(InvalidInputException.class, () -> savedArticleService.saveArticle(dto));
    }

    @Test
    @DisplayName("saveArticle throws if user not found")
    void saveArticle_ThrowsIfUserNotFound() {
        SavedArticleRequestDTO dto = new SavedArticleRequestDTO(1L, 2L);
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> savedArticleService.saveArticle(dto));
    }

    @Test
    @DisplayName("saveArticle throws if article not found")
    void saveArticle_ThrowsIfArticleNotFound() {
        SavedArticleRequestDTO dto = new SavedArticleRequestDTO(1L, 2L);
        User user = new User(); user.setId(1L);
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Mockito.when(articleRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(ArticleNotFoundException.class, () -> savedArticleService.saveArticle(dto));
    }

    @Test
    @DisplayName("getSavedArticlesByUserId returns list of responses")
    void getSavedArticlesByUserId_ReturnsList() {
        User user = new User(); user.setId(1L);
        Article article = new Article(); article.setId(2L); article.setTitle("title");
        article.setDescription("desc"); article.setUrl("url"); article.setContent("content");
        article.setPublishedAt(LocalDateTime.now());
        SavedArticle savedArticle = new SavedArticle(10L, LocalDateTime.now(), article, user);
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Mockito.when(savedArticleRepository.findByUserOrderBySavedAtDesc(user)).thenReturn(List.of(savedArticle));
        List<SavedArticleResponseDTO> result = savedArticleService.getSavedArticlesByUserId(1L);
        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).getSavedArticleId());
        assertEquals("title", result.get(0).getTitle());
    }

    @Test
    @DisplayName("getSavedArticlesByUserId throws if user not found")
    void getSavedArticlesByUserId_ThrowsIfUserNotFound() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> savedArticleService.getSavedArticlesByUserId(1L));
    }

    @Test
    @DisplayName("deleteSavedArticle deletes if found and user matches")
    void deleteSavedArticle_DeletesIfFound() {
        User user = new User(); user.setId(1L);
        SavedArticle savedArticle = new SavedArticle();
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Mockito.when(savedArticleRepository.findBySavedArticleIdAndUser(2L, user)).thenReturn(Optional.of(savedArticle));
        Mockito.doNothing().when(savedArticleRepository).delete(savedArticle);
        assertDoesNotThrow(() -> savedArticleService.deleteSavedArticle(2L, 1L));
    }

    @Test
    @DisplayName("deleteSavedArticle throws if user not found")
    void deleteSavedArticle_ThrowsIfUserNotFound() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> savedArticleService.deleteSavedArticle(2L, 1L));
    }

    @Test
    @DisplayName("deleteSavedArticle throws if saved article not found or not owned by user")
    void deleteSavedArticle_ThrowsIfNotFoundOrNotOwned() {
        User user = new User(); user.setId(1L);
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Mockito.when(savedArticleRepository.findBySavedArticleIdAndUser(2L, user)).thenReturn(Optional.empty());
        assertThrows(InvalidInputException.class, () -> savedArticleService.deleteSavedArticle(2L, 1L));
    }
}
