package com.itt.newsaggregator.service;

import com.itt.newsaggregator.dto.NotificationDTO;
import com.itt.newsaggregator.dto.NotificationSettingsDTO;
import com.itt.newsaggregator.dto.UsersKeywordsMappingDTO;
import com.itt.newsaggregator.entities.*;
import com.itt.newsaggregator.repository.CategoryArticleMappingRepository;
import com.itt.newsaggregator.repository.CategoryRepository;
import com.itt.newsaggregator.repository.KeywordRepository;
import com.itt.newsaggregator.repository.NotificationRepository;
import com.itt.newsaggregator.repository.NotificationSettingRepository;
import com.itt.newsaggregator.repository.UserKeywordMappingRepository;
import com.itt.newsaggregator.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationSettingRepository notificationSettingRepository;
    private final UserKeywordMappingRepository userKeywordMappingRepository;
    private final KeywordRepository keywordRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryArticleMappingRepository categoryArticleMappingRepository;
    private final EmailService emailService;

    public NotificationService(NotificationRepository notificationRepository,
                               NotificationSettingRepository notificationSettingRepository,
                               UserKeywordMappingRepository userKeywordMappingRepository,
                               KeywordRepository keywordRepository,
                               UserRepository userRepository,
                               CategoryRepository categoryRepository,
                               CategoryArticleMappingRepository categoryArticleMappingRepository,
                               EmailService emailService) {
        this.notificationRepository = notificationRepository;
        this.notificationSettingRepository = notificationSettingRepository;
        this.userKeywordMappingRepository = userKeywordMappingRepository;
        this.keywordRepository = keywordRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.categoryArticleMappingRepository = categoryArticleMappingRepository;
        this.emailService = emailService;
    }

    public List<NotificationDTO> getNotificationsByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Only return unread notifications
        List<Notification> notifications = notificationRepository.findByUserAndIsReadOrderByCreatedAtDesc(user, false);
        return notifications.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public void markNotificationAsRead(Long notificationId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getUser().getId().equals(userId)) {
            throw new RuntimeException("You don't have permission to read this notification");
        }

        notification.setIsRead(true);
        notification.setReadAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    public NotificationSettingsDTO getNotificationSettings(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Get category settings
        List<NotificationSetting> categorySettings = notificationSettingRepository.findByUser(user);
        Map<String, Boolean> categoryMap = new HashMap<>();

        // Get all categories and set default values
        List<Category> allCategories = categoryRepository.findAll();
        for (Category category : allCategories) {
            categoryMap.put(category.getName(), false); // default to disabled
        }

        // Update with actual settings
        for (NotificationSetting setting : categorySettings) {
            categoryMap.put(setting.getCategory().getName(), setting.isEnabled());
        }

        // Get keywords
        List<UserKeywordMapping> userKeywordMappings = userKeywordMappingRepository.findByUserAndIsEnable(user, true);
        List<String> keywords = userKeywordMappings.stream()
                .map(mapping -> mapping.getKeyword().getKeyword())
                .collect(Collectors.toList());

        return new NotificationSettingsDTO(userId, categoryMap, keywords);
    }

    public void updateNotificationSettings(NotificationSettingsDTO dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update category settings
        for (Map.Entry<String, Boolean> entry : dto.getCategorySettings().entrySet()) {
            String categoryName = entry.getKey();
            Boolean enabled = entry.getValue();

            Category category = categoryRepository.findByNameIgnoreCase(categoryName)
                    .orElseThrow(() -> new RuntimeException("Category not found: " + categoryName));

            Optional<NotificationSetting> existingSetting = notificationSettingRepository.findByUserAndCategory(user, category);

            if (existingSetting.isPresent()) {
                NotificationSetting setting = existingSetting.get();
                setting.setEnabled(enabled);
                notificationSettingRepository.save(setting);
            } else if (enabled) {
                // Only create if enabled
                NotificationSetting newSetting = new NotificationSetting();
                newSetting.setUser(user);
                newSetting.setCategory(category);
                newSetting.setEnabled(true);
                notificationSettingRepository.save(newSetting);
            }
        }

        // Update keywords - first delete existing mappings
        List<UserKeywordMapping> existingMappings = userKeywordMappingRepository.findByUser(user);
        userKeywordMappingRepository.deleteAll(existingMappings);

        // Add new keyword mappings
        if (dto.getKeywords() != null) {
            for (String keywordText : dto.getKeywords()) {
                if (keywordText != null && !keywordText.trim().isEmpty()) {
                    // Find or create keyword
                    Keyword keyword = findOrCreateKeyword(keywordText.trim());

                    // Create user-keyword mapping
                    UserKeywordMapping mapping = new UserKeywordMapping();
                    mapping.setUser(user);
                    mapping.setKeyword(keyword);
                    mapping.setEnable(true);
                    userKeywordMappingRepository.save(mapping);
                }
            }
        }
    }

    private Keyword findOrCreateKeyword(String keywordText) {
        Optional<Keyword> existingKeyword = keywordRepository.findByKeyword(keywordText);
        if (existingKeyword.isPresent()) {
            return existingKeyword.get();
        } else {
            Keyword newKeyword = new Keyword();
            newKeyword.setKeyword(keywordText);
            return keywordRepository.save(newKeyword);
        }
    }

    public void updateUserKeywords(UsersKeywordsMappingDTO dto) {
        User user = userRepository.findById(Long.parseLong(dto.getUserId()))
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Delete existing keyword mappings
        List<UserKeywordMapping> existingMappings = userKeywordMappingRepository.findByUser(user);
        userKeywordMappingRepository.deleteAll(existingMappings);

        // Add new keyword mappings
        if (dto.getKeywordIds() != null) {
            for (String keywordText : dto.getKeywordIds()) {
                if (keywordText != null && !keywordText.trim().isEmpty()) {
                    // Find or create keyword
                    Keyword keyword = findOrCreateKeyword(keywordText.trim());

                    // Create user-keyword mapping
                    UserKeywordMapping mapping = new UserKeywordMapping();
                    mapping.setUser(user);
                    mapping.setKeyword(keyword);
                    mapping.setEnable(dto.isEnable());
                    userKeywordMappingRepository.save(mapping);
                }
            }
        }
    }

    public void createNotificationForArticle(Article article) {
        System.out.println("🔔 Creating notifications for article: " + article.getTitle());

        // Get all users with enabled category notifications
        List<CategoryArticleMapping> categoryMappings = categoryArticleMappingRepository.findByArticle(article);
        System.out.println("📂 Found " + categoryMappings.size() + " category mappings for article");

        for (CategoryArticleMapping mapping : categoryMappings) {
            Category category = mapping.getCategory();
            System.out.println("🏷️ Checking category: " + category.getName());

            List<NotificationSetting> enabledSettings = notificationSettingRepository.findByCategoryAndEnabled(category, true);
            System.out.println("👥 Found " + enabledSettings.size() + " users with enabled notifications for category: " + category.getName());

            for (NotificationSetting setting : enabledSettings) {
                System.out.println("📧 Creating notification for user: " + setting.getUser().getUsername());
                createNotification(setting.getUser(), article, "category", category.getName());
            }
        }

        // Get all users with enabled keywords and check for matches
        List<UserKeywordMapping> allKeywordMappings = userKeywordMappingRepository.findByIsEnable(true);
        System.out.println("🔍 Checking " + allKeywordMappings.size() + " keyword mappings");

        for (UserKeywordMapping mapping : allKeywordMappings) {
            String keyword = mapping.getKeyword().getKeyword();
            System.out.println("🔤 Checking keyword: " + keyword);

            if (articleMatchesKeyword(article, keyword)) {
                System.out.println("✅ Keyword match found! Creating notification for user: " + mapping.getUser().getUsername());
                createNotification(mapping.getUser(), article, "keyword", keyword);
            }
        }

        System.out.println("✅ Finished creating notifications for article: " + article.getTitle());
    }

    private boolean articleMatchesKeyword(Article article, String keyword) {
        String lowerKeyword = keyword.toLowerCase();
        String lowerTitle = article.getTitle().toLowerCase();
        String lowerDescription = article.getDescription() != null ? article.getDescription().toLowerCase() : "";
        String lowerContent = article.getContent() != null ? article.getContent().toLowerCase() : "";

        // Use word boundaries for more precise matching
        String wordBoundaryPattern = "\\b" + java.util.regex.Pattern.quote(lowerKeyword) + "\\b";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(wordBoundaryPattern);

        return pattern.matcher(lowerTitle).find() ||
                pattern.matcher(lowerDescription).find() ||
                pattern.matcher(lowerContent).find();
    }

    private void createNotification(User user, Article article, String type, String triggerValue) {
        // Check if notification already exists
        boolean exists = notificationRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .anyMatch(n -> n.getArticle().getId().equals(article.getId()) &&
                        n.getNotificationType().equals(type) &&
                        n.getTriggerValue().equals(triggerValue));

        if (!exists) {
            Notification notification = new Notification();
            notification.setUser(user);
            notification.setArticle(article);
            notification.setNotificationType(type);
            notification.setTriggerValue(triggerValue);
            notification.setMessage("New article matching your " + type + " preference: " + triggerValue);
            notification.setIsRead(false);
            notification.setEmailSent(false);
            notification.setCreatedAt(LocalDateTime.now());
            notification.setSendAt(LocalDateTime.now());

            // Set category mapping only for category notifications
            if ("category".equals(type)) {
                List<CategoryArticleMapping> mappings = categoryArticleMappingRepository.findByArticle(article);
                CategoryArticleMapping categoryMapping = mappings.stream()
                        .filter(mapping -> mapping.getCategory().getName().equalsIgnoreCase(triggerValue))
                        .findFirst()
                        .orElse(null);
                notification.setCategoryMapping(categoryMapping);
            }
            // For keyword notifications, categoryMapping remains null (which is fine)

            notificationRepository.save(notification);
        }
    }

    private NotificationDTO convertToDTO(Notification notification) {
        return new NotificationDTO(
                notification.getId(),
                notification.getArticle().getId(),
                notification.getArticle().getTitle(),
                notification.getArticle().getDescription(),
                notification.getArticle().getUrl(),
                notification.getNotificationType(),
                notification.getTriggerValue(),
                notification.getIsRead(),
                notification.getCreatedAt(),
                notification.getReadAt()
        );
    }
}
