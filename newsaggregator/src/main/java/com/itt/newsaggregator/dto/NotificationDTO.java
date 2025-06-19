package com.itt.newsaggregator.dto;

import java.time.LocalDateTime;

public class NotificationDTO {
    private String notificationId;
    private String message;
    private boolean isRead;
    private LocalDateTime createdAt;
    private LocalDateTime sendAt;
}
