package com.itt.newsaggregator.service;

import com.itt.newsaggregator.dto.ArticleDTO;
import com.itt.newsaggregator.entities.Article;
import com.itt.newsaggregator.entities.CategoryArticleMapping;
import com.itt.newsaggregator.entities.User;
import com.itt.newsaggregator.Enums.ArticleStatus;
import com.itt.newsaggregator.exception.ArticleNotFoundException;
import com.itt.newsaggregator.mapper.ArticleMapper;
import com.itt.newsaggregator.repository.ArticleRepository;
import com.itt.newsaggregator.repository.CategoryArticleMappingRepository;
import com.itt.newsaggregator.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

class ArticleServiceTest {
    @Mock
    private CategoryArticleMappingRepository mappingRepo;
    @Mock
    private ArticleMapper articleMapper;
    @Mock
    private ArticleRepository articleRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CategoryService categoryService;
    @Mock
    private KeywordService keywordService;
    private ArticleService articleService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        articleService = new ArticleService(mappingRepo, articleMapper, articleRepository, userRepository, categoryService, keywordService);
    }

    @Test
    @DisplayName("searchArticles returns filtered and sorted list")
    void searchArticles_ReturnsList() {
        Article article = new Article();
        article.setId(1L);
        Mockito.when(articleRepository.searchArticlesWithoutDateRange(anyString())).thenReturn(List.of(article));
        Mockito.when(articleMapper.toDto(any(Article.class))).thenReturn(new ArticleDTO());
        Mockito.when(mappingRepo.findByArticle(any(Article.class))).thenReturn(Collections.emptyList());
        List<ArticleDTO> result = articleService.searchArticles("query", null, null, null);
        assertNotNull(result);
    }

    @Test
    @DisplayName("reportArticle increments report count and hides if threshold reached")
    void reportArticle_AutoHides() {
        Article article = new Article();
        article.setId(1L);
        article.setReportCount(4);
        article.setStatus(ArticleStatus.PUBLIC);
        User user = new User();
        user.setId(1L);
        Mockito.when(articleRepository.findById(anyLong())).thenReturn(Optional.of(article));
        Mockito.when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        Mockito.when(articleRepository.save(any(Article.class))).thenReturn(article);
        articleService.reportArticle(1L, 1L, "reason");
        assertEquals(ArticleStatus.HIDDEN, article.getStatus());
        assertTrue(article.getReportCount() >= 5);
    }

    @Test
    @DisplayName("toggleArticleVisibility hides and unhides article")
    void toggleArticleVisibility_HideUnhide() {
        Article article = new Article();
        article.setId(1L);
        article.setStatus(ArticleStatus.PUBLIC);
        User admin = new User();
        admin.setId(1L);
        Mockito.when(articleRepository.findById(anyLong())).thenReturn(Optional.of(article));
        Mockito.when(userRepository.findById(anyLong())).thenReturn(Optional.of(admin));
        Mockito.when(articleRepository.save(any(Article.class))).thenReturn(article);
        articleService.toggleArticleVisibility(1L, 1L, true, "reason");
        assertEquals(ArticleStatus.HIDDEN, article.getStatus());
        articleService.toggleArticleVisibility(1L, 1L, false, "reason");
        assertEquals(ArticleStatus.PUBLIC, article.getStatus());
    }

    @Test
    @DisplayName("getArticleById throws if not found")
    void getArticleById_ThrowsIfNotFound() {
        Mockito.when(articleRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(ArticleNotFoundException.class, () -> articleService.getArticleById(1L));
    }
}
