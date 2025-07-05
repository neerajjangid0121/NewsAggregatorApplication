package com.itt.newsaggregator.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.itt.newsaggregator.entities.Article;

public interface ArticleRepository extends JpaRepository<Article,Long> {
    boolean existsByUrl(String url);
}
