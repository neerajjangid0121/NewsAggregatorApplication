package com.itt.newsaggregator.repository;

import com.itt.newsaggregator.entities.User;
import com.itt.newsaggregator.entities.UserKeywordMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserKeywordMappingRepository extends JpaRepository<UserKeywordMapping, Long> {
    List<UserKeywordMapping> findByUser(User user);
    List<UserKeywordMapping> findByUserAndIsEnable(User user, boolean isEnable);
    List<UserKeywordMapping> findByIsEnable(boolean isEnable);
}