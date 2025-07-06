package com.itt.newsaggregator.repository;

import com.itt.newsaggregator.entities.Notification;
import com.itt.newsaggregator.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserOrderByCreatedAtDesc(User user);
    List<Notification> findByUserAndIsReadOrderByCreatedAtDesc(User user, Boolean isRead);

    @Query("SELECT n FROM Notification n WHERE n.emailSent = false")
    List<Notification> findUnsentEmailNotifications();

    @Query("SELECT n FROM Notification n WHERE n.user = :user AND n.isRead = false")
    List<Notification> findUnreadNotificationsByUser(@Param("user") User user);
}