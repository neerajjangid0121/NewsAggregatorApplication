package com.itt.newsaggregator.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    @Column(name = "notification_type", nullable = false)
    private String notificationType; // "category" or "keyword"

    @Column(name = "trigger_value", nullable = false)
    private String triggerValue; // category name or keyword that triggered the notification

    @Column(name = "message", nullable = false)
    private String message;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    @Column(name = "email_sent", nullable = false)
    private Boolean emailSent = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "send_at")
    private LocalDateTime sendAt;

    @ManyToOne
    @JoinColumn(name = "category_mapping_id", nullable = true)
    private CategoryArticleMapping categoryMapping;
}
