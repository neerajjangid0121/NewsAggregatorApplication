package com.itt.newsaggregator.repository;

import com.itt.newsaggregator.entities.Article;
import com.itt.newsaggregator.entities.SavedArticle;
import com.itt.newsaggregator.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SavedArticleRepository extends JpaRepository<SavedArticle, Long> {
    boolean existsByUserAndArticle(User user, Article article);
}
