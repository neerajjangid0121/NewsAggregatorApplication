package com.itt.newsaggregator.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.itt.newsaggregator.entities.Article;

import java.time.LocalDateTime;
import java.util.List;

public interface ArticleRepository extends JpaRepository<Article, Long> {
    boolean existsByUrl(String url);

    @Query("SELECT a FROM Article a WHERE " +
            "(LOWER(a.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(a.description) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(a.content) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
            "a.publishedAt BETWEEN :startDate AND :endDate AND " +
            "a.status = 'PUBLIC' " +
            "ORDER BY a.publishedAt DESC")
    List<Article> searchArticles(@Param("query") String query,
                                 @Param("startDate") LocalDateTime startDate,
                                 @Param("endDate") LocalDateTime endDate);

    @Query("SELECT a FROM Article a WHERE " +
            "(LOWER(a.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(a.description) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(a.content) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
            "a.status = 'PUBLIC' " +
            "ORDER BY a.publishedAt DESC")
    List<Article> searchArticlesWithoutDateRange(@Param("query") String query);

    List<Article> findByStatus(com.itt.newsaggregator.Enums.ArticleStatus status);

    List<Article> findByStatusOrderByPublishedAtDesc(com.itt.newsaggregator.Enums.ArticleStatus status);

    @Query("SELECT a FROM Article a WHERE a.reportCount >= :threshold ORDER BY a.reportCount DESC, a.lastReportedAt DESC")
    List<Article> findArticlesWithHighReportCount(@Param("threshold") int threshold);

    @Query("SELECT a FROM Article a WHERE a.status = 'HIDDEN' ORDER BY a.hiddenAt DESC")
    List<Article> findHiddenArticlesOrderByHiddenAt();

    List<Article> findByReportCountGreaterThan(int count);
}
