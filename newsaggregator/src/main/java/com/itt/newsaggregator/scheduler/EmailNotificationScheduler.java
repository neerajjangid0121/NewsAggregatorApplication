package com.itt.newsaggregator.scheduler;

import com.itt.newsaggregator.entities.Notification;
import com.itt.newsaggregator.repository.NotificationRepository;
import com.itt.newsaggregator.service.EmailService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class EmailNotificationScheduler {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    public EmailNotificationScheduler(NotificationRepository notificationRepository,
                                      EmailService emailService) {
        this.notificationRepository = notificationRepository;
        this.emailService = emailService;
    }

    @Scheduled(fixedRate = 300000) // Run every 5 minutes
    public void sendPendingEmails() {
        System.out.println("Sending pending email notifications at: " + LocalDateTime.now());

        List<Notification> unsentNotifications = notificationRepository.findUnsentEmailNotifications();
        System.out.println("Found " + unsentNotifications.size() + " unsent notifications");

        if (!unsentNotifications.isEmpty()) {
            System.out.println("Sample notification - ID: " + unsentNotifications.get(0).getId() +
                    ", EmailSent: " + unsentNotifications.get(0).getEmailSent() +
                    ", User: " + unsentNotifications.get(0).getUser().getUsername());
        }

        for (Notification notification : unsentNotifications) {
            try {
                emailService.sendNotificationEmail(notification);
                notification.setEmailSent(true);
                notificationRepository.save(notification);
                System.out.println("Email sent for notification ID: " + notification.getId());
            } catch (Exception e) {
                System.err.println("Failed to send email for notification ID: " + notification.getId() + " - " + e.getMessage());
            }
        }

        System.out.println("Email notification scheduler completed. Processed: " + unsentNotifications.size() + " notifications");
    }
}