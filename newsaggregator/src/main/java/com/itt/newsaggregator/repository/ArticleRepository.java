package com.itt.newsaggregator.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.itt.newsaggregator.entities.Article;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ArticleRepository extends JpaRepository<Article,Long> {
    boolean existsByUrl(String url);
    @Query("SELECT a FROM Article a WHERE " +
            "(LOWER(a.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(a.description) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(a.content) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
            "a.publishedAt BETWEEN :startDate AND :endDate " +
            "ORDER BY a.publishedAt DESC")
    List<Article> searchArticles(@Param("query") String query,
                                 @Param("startDate") LocalDateTime startDate,
                                 @Param("endDate") LocalDateTime endDate);

    @Query("SELECT a FROM Article a WHERE " +
            "(LOWER(a.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(a.description) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(a.content) LIKE LOWER(CONCAT('%', :query, '%'))) " +
            "ORDER BY a.publishedAt DESC")
    List<Article> searchArticlesWithoutDateRange(@Param("query") String query);
}
