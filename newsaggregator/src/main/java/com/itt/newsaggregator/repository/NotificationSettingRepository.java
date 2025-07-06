package com.itt.newsaggregator.repository;

import com.itt.newsaggregator.entities.Category;
import com.itt.newsaggregator.entities.NotificationSetting;
import com.itt.newsaggregator.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationSettingRepository extends JpaRepository<NotificationSetting, Long> {
    List<NotificationSetting> findByUser(User user);
    List<NotificationSetting> findByUserAndEnabled(User user, boolean enabled);
    Optional<NotificationSetting> findByUserAndCategory(User user, Category category);
    List<NotificationSetting> findByCategoryAndEnabled(Category category, boolean enabled);
}