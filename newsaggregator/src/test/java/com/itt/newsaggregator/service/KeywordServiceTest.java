package com.itt.newsaggregator.service;

import com.itt.newsaggregator.dto.KeywordDTO;
import com.itt.newsaggregator.entities.Keyword;
import com.itt.newsaggregator.entities.User;
import com.itt.newsaggregator.exception.KeywordNotFoundException;
import com.itt.newsaggregator.exception.UserNotFoundException;
import com.itt.newsaggregator.repository.KeywordRepository;
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

class KeywordServiceTest {
    @Mock
    private KeywordRepository keywordRepository;
    @Mock
    private UserRepository userRepository;
    private KeywordService keywordService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        keywordService = new KeywordService(keywordRepository, userRepository);
    }

    @Test
    @DisplayName("restrictKeyword restricts existing keyword")
    void restrictKeyword_RestrictsExisting() {
        User admin = new User();
        admin.setId(1L);
        Keyword keyword = new Keyword();
        keyword.setKeyword("test");
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        Mockito.when(keywordRepository.findByKeyword("test")).thenReturn(Optional.of(keyword));
        Mockito.when(keywordRepository.save(any(Keyword.class))).thenReturn(keyword);
        keywordService.restrictKeyword("test", 1L, "reason");
        assertTrue(keyword.getIsRestricted());
        assertEquals(admin.getId(), keyword.getRestrictedBy());
        assertEquals("reason", keyword.getRestrictionReason());
    }

    @Test
    @DisplayName("restrictKeyword creates and restricts new keyword")
    void restrictKeyword_CreatesNew() {
        User admin = new User();
        admin.setId(1L);
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        Mockito.when(keywordRepository.findByKeyword("newword")).thenReturn(Optional.empty());
        Mockito.when(keywordRepository.save(any(Keyword.class))).thenAnswer(invocation -> invocation.getArgument(0));
        keywordService.restrictKeyword("newword", 1L, "reason");
        Mockito.verify(keywordRepository).save(any(Keyword.class));
    }

    @Test
    @DisplayName("restrictKeyword throws if admin not found")
    void restrictKeyword_ThrowsIfAdminNotFound() {
        Mockito.when(userRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> keywordService.restrictKeyword("test", 1L, "reason"));
    }

    @Test
    @DisplayName("unrestrictKeyword clears restriction fields")
    void unrestrictKeyword_ClearsFields() {
        Keyword keyword = new Keyword();
        keyword.setKeyword("test");
        keyword.setIsRestricted(true);
        keyword.setRestrictedBy(1L);
        keyword.setRestrictionReason("reason");
        Mockito.when(keywordRepository.findByKeyword("test")).thenReturn(Optional.of(keyword));
        Mockito.when(keywordRepository.save(any(Keyword.class))).thenReturn(keyword);
        keywordService.unrestrictKeyword("test");
        assertFalse(keyword.getIsRestricted());
        assertNull(keyword.getRestrictedBy());
        assertNull(keyword.getRestrictionReason());
    }

    @Test
    @DisplayName("unrestrictKeyword throws if not found")
    void unrestrictKeyword_ThrowsIfNotFound() {
        Mockito.when(keywordRepository.findByKeyword(anyString())).thenReturn(Optional.empty());
        assertThrows(KeywordNotFoundException.class, () -> keywordService.unrestrictKeyword("test"));
    }

    @Test
    @DisplayName("getRestrictedKeywords returns DTO list")
    void getRestrictedKeywords_ReturnsList() {
        Keyword keyword = new Keyword();
        keyword.setKeyId(1L);
        keyword.setKeyword("test");
        keyword.setIsRestricted(true);
        Mockito.when(keywordRepository.findByIsRestrictedTrue()).thenReturn(List.of(keyword));
        List<KeywordDTO> result = keywordService.getRestrictedKeywords();
        assertEquals(1, result.size());
        assertEquals("test", result.get(0).getKeyword());
        assertTrue(result.get(0).getIsRestricted());
    }

    @Test
    @DisplayName("getAllKeywords returns DTO list")
    void getAllKeywords_ReturnsList() {
        Keyword keyword = new Keyword();
        keyword.setKeyId(1L);
        keyword.setKeyword("test");
        keyword.setIsRestricted(false);
        Mockito.when(keywordRepository.findAll()).thenReturn(List.of(keyword));
        List<KeywordDTO> result = keywordService.getAllKeywords();
        assertEquals(1, result.size());
        assertEquals("test", result.get(0).getKeyword());
        assertFalse(result.get(0).getIsRestricted());
    }

    @Test
    @DisplayName("isKeywordRestricted returns true if restricted")
    void isKeywordRestricted_ReturnsTrue() {
        Keyword keyword = new Keyword();
        keyword.setIsRestricted(true);
        Mockito.when(keywordRepository.findByKeyword(anyString())).thenReturn(Optional.of(keyword));
        assertTrue(keywordService.isKeywordRestricted("test"));
    }

    @Test
    @DisplayName("isKeywordRestricted returns false if not found")
    void isKeywordRestricted_ReturnsFalseIfNotFound() {
        Mockito.when(keywordRepository.findByKeyword(anyString())).thenReturn(Optional.empty());
        assertFalse(keywordService.isKeywordRestricted("test"));
    }

    @Test
    @DisplayName("hasRestrictedKeywords returns true if match found")
    void hasRestrictedKeywords_ReturnsTrueIfMatch() {
        KeywordDTO dto = new KeywordDTO("1", "badword", true);
        KeywordService spyService = Mockito.spy(keywordService);
        Mockito.doReturn(List.of(dto)).when(spyService).getRestrictedKeywords();
        boolean result = spyService.hasRestrictedKeywords("badword in title", null, null);
        assertTrue(result);
    }

    @Test
    @DisplayName("hasRestrictedKeywords returns false if no match")
    void hasRestrictedKeywords_ReturnsFalseIfNoMatch() {
        KeywordDTO dto = new KeywordDTO("1", "badword", true);
        KeywordService spyService = Mockito.spy(keywordService);
        Mockito.doReturn(List.of(dto)).when(spyService).getRestrictedKeywords();
        boolean result = spyService.hasRestrictedKeywords("good title", "good desc", "good content");
        assertFalse(result);
    }
}