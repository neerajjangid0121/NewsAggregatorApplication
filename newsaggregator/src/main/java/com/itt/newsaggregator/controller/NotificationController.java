package com.itt.newsaggregator.controller;

import com.itt.newsaggregator.dto.NotificationDTO;
import com.itt.newsaggregator.dto.NotificationSettingsDTO;
import com.itt.newsaggregator.dto.UsersKeywordsMappingDTO;
import com.itt.newsaggregator.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<NotificationDTO>> getNotifications(@PathVariable Long userId) {
        List<NotificationDTO> notifications = notificationService.getNotificationsByUserId(userId);
        return ResponseEntity.ok(notifications);
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<Void> markNotificationAsRead(
            @PathVariable Long notificationId,
            @RequestParam Long userId) {
        notificationService.markNotificationAsRead(notificationId, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/settings/{userId}")
    public ResponseEntity<NotificationSettingsDTO> getNotificationSettings(@PathVariable Long userId) {
        NotificationSettingsDTO settings = notificationService.getNotificationSettings(userId);
        return ResponseEntity.ok(settings);
    }

    @PostMapping("/settings")
    public ResponseEntity<Void> updateNotificationSettings(@RequestBody NotificationSettingsDTO settingsDTO) {
        notificationService.updateNotificationSettings(settingsDTO);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/keywords")
    public ResponseEntity<Void> updateNotificationKeywords(@RequestBody Map<String, Object> request) {
        Long userId = Long.parseLong(request.get("user_id").toString());
        List<String> keywords = (List<String>) request.get("keywords");
        NotificationSettingsDTO dto = new NotificationSettingsDTO();
        dto.setUserId(userId);
        dto.setKeywords(keywords);

        notificationService.updateNotificationSettings(dto);
        return ResponseEntity.ok().build();
    }
}
