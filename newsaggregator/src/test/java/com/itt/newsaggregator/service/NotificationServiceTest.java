package com.itt.newsaggregator.service;

import com.itt.newsaggregator.dto.NotificationDTO;
import com.itt.newsaggregator.dto.NotificationSettingsDTO;
import com.itt.newsaggregator.entities.*;
import com.itt.newsaggregator.repository.*;
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

class NotificationServiceTest {
    @Mock private NotificationRepository notificationRepository;
    @Mock private NotificationSettingRepository notificationSettingRepository;
    @Mock private UserKeywordMappingRepository userKeywordMappingRepository;
    @Mock private KeywordRepository keywordRepository;
    @Mock private UserRepository userRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private CategoryArticleMappingRepository categoryArticleMappingRepository;
    @Mock private EmailService emailService;
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        notificationService = new NotificationService(
                notificationRepository,
                notificationSettingRepository,
                userKeywordMappingRepository,
                keywordRepository,
                userRepository,
                categoryRepository,
                categoryArticleMappingRepository,
                emailService
        );
    }

    @Test
    @DisplayName("markNotificationAsRead marks as read if user matches")
    void markNotificationAsRead_Success() {
        User user = new User(); user.setId(1L);
        Notification notification = new Notification();
        notification.setId(2L); notification.setUser(user); notification.setIsRead(false);
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Mockito.when(notificationRepository.findById(2L)).thenReturn(Optional.of(notification));
        Mockito.when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        notificationService.markNotificationAsRead(2L, 1L);
        assertTrue(notification.getIsRead());
        assertNotNull(notification.getReadAt());
    }

    @Test
    @DisplayName("markNotificationAsRead throws if user mismatch")
    void markNotificationAsRead_ThrowsIfUserMismatch() {
        User user = new User(); user.setId(1L);
        User otherUser = new User(); otherUser.setId(2L);
        Notification notification = new Notification();
        notification.setId(2L); notification.setUser(otherUser); notification.setIsRead(false);
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Mockito.when(notificationRepository.findById(2L)).thenReturn(Optional.of(notification));
        assertThrows(RuntimeException.class, () -> notificationService.markNotificationAsRead(2L, 1L));
    }

    @Test
    @DisplayName("getNotificationSettings returns settings DTO")
    void getNotificationSettings_ReturnsDTO() {
        User user = new User(); user.setId(1L);
        Category cat = new Category(); cat.setName("Tech");
        NotificationSetting setting = new NotificationSetting();
        setting.setCategory(cat); setting.setEnabled(true);
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Mockito.when(notificationSettingRepository.findByUser(user)).thenReturn(List.of(setting));
        Mockito.when(categoryRepository.findAll()).thenReturn(List.of(cat));
        UserKeywordMapping mapping = new UserKeywordMapping();
        Keyword keyword = new Keyword(); keyword.setKeyword("java");
        mapping.setKeyword(keyword);
        Mockito.when(userKeywordMappingRepository.findByUserAndIsEnable(user, true)).thenReturn(List.of(mapping));
        NotificationSettingsDTO dto = notificationService.getNotificationSettings(1L);
        assertEquals(1L, dto.getUserId());
        assertTrue(dto.getCategorySettings().get("Tech"));
        assertTrue(dto.getKeywords().contains("java"));
    }

    @Test
    @DisplayName("updateNotificationSettings updates category and keywords")
    void updateNotificationSettings_UpdatesSettings() {
        User user = new User(); user.setId(1L);
        Category cat = new Category(); cat.setName("Tech");
        NotificationSettingsDTO dto = new NotificationSettingsDTO();
        dto.setUserId(1L);
        Map<String, Boolean> catSettings = new HashMap<>();
        catSettings.put("Tech", true);
        dto.setCategorySettings(catSettings);
        dto.setKeywords(List.of("java"));
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Mockito.when(categoryRepository.findByNameIgnoreCase("Tech")).thenReturn(Optional.of(cat));
        Mockito.when(notificationSettingRepository.findByUserAndCategory(user, cat)).thenReturn(Optional.empty());
        Mockito.when(userKeywordMappingRepository.findByUser(user)).thenReturn(Collections.emptyList());
        Mockito.when(keywordRepository.findByKeyword("java")).thenReturn(Optional.empty());
        Mockito.when(keywordRepository.save(any(Keyword.class))).thenAnswer(invocation -> invocation.getArgument(0));
        notificationService.updateNotificationSettings(dto);
        Mockito.verify(notificationSettingRepository).save(any(NotificationSetting.class));
        Mockito.verify(userKeywordMappingRepository).save(any(UserKeywordMapping.class));
    }
}
