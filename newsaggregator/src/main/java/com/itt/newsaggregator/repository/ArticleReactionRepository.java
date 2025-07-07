package com.itt.newsaggregator.repository;

import com.itt.newsaggregator.entities.Article;
import com.itt.newsaggregator.entities.ArticleReaction;
import com.itt.newsaggregator.entities.User;
import com.itt.newsaggregator.Enums.ReactionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ArticleReactionRepository extends JpaRepository<ArticleReaction, Long> {
    Optional<ArticleReaction> findByUserAndArticle(User user, Article article);
    int countByArticleAndReactionType(Article article, ReactionType reactionType);
}
