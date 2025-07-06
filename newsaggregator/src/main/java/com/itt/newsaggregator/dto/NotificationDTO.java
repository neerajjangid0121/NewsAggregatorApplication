package com.itt.newsaggregator.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationDTO {
    private Long id;
    private Long articleId;
    private String articleTitle;
    private String articleDescription;
    private String articleUrl;
    private String notificationType;
    private String triggerValue;
    private Boolean isRead;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
}
