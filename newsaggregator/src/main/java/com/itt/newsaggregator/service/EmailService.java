package com.itt.newsaggregator.service;

import com.itt.newsaggregator.entities.Notification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendNotificationEmail(Notification notification) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(notification.getUser().getEmail());
            helper.setSubject("News Notification: " + notification.getArticle().getTitle());
            helper.setText(createEmailContent(notification), true);

            mailSender.send(message);
            System.out.println("✅ Email sent successfully to: " + notification.getUser().getEmail());
        } catch (MessagingException e) {
            System.err.println("❌ Failed to send email: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("❌ Unexpected error sending email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String createEmailContent(Notification notification) {
        return """
            <html>
            <body>
                <h2>News Notification</h2>
                <p>Hello <strong>%s</strong>,</p>
                <p>You have a new notification based on your preferences:</p>
                <div style="border: 1px solid #ddd; padding: 15px; margin: 10px 0; border-radius: 5px;">
                    <h3>%s</h3>
                    <p>%s</p>
                    <p><strong>Triggered by:</strong> %s (%s)</p>
                    <p><a href="%s" style="background-color: #007bff; color: white; padding: 10px 15px; text-decoration: none; border-radius: 3px;">Read Full Article</a></p>
                </div>
                <p>Best regards,<br>News Aggregator Team</p>
            </body>
            </html>
            """.formatted(
                notification.getUser().getUsername(),
                notification.getArticle().getTitle(),
                notification.getArticle().getDescription(),
                notification.getTriggerValue(),
                notification.getNotificationType(),
                notification.getArticle().getUrl()
        );
    }
}