package com.itt.newsaggregator.repository;

import com.itt.newsaggregator.entities.UserArticleReadHistory;
import com.itt.newsaggregator.entities.User;
import com.itt.newsaggregator.entities.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserArticleReadHistoryRepository extends JpaRepository<UserArticleReadHistory, Long> {
    List<UserArticleReadHistory> findByUser(User user);
    List<UserArticleReadHistory> findByUserAndArticle(User user, Article article);
}