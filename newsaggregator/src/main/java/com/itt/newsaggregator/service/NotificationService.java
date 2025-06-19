package com.itt.newsaggregator.service;

import com.itt.newsaggregator.entities.Notification;
import com.itt.newsaggregator.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepo;

    public List<Notification> getUnreadForUser(Long userId) {
        return notificationRepo.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
    }
}
