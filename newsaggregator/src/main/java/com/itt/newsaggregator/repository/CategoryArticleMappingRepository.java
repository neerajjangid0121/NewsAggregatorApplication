package com.itt.newsaggregator.repository;

import com.itt.newsaggregator.entities.CategoryArticleMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface CategoryArticleMappingRepository extends JpaRepository<CategoryArticleMapping, Long> {

    @Query("SELECT cam FROM CategoryArticleMapping cam " +
            "JOIN cam.article a " +
            "JOIN cam.category c " +
            "WHERE a.publishedAt BETWEEN :start AND :end")
    List<CategoryArticleMapping> findByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT cam FROM CategoryArticleMapping cam " +
            "JOIN cam.article a " +
            "JOIN cam.category c " +
            "WHERE a.publishedAt BETWEEN :start AND :end " +
            "AND LOWER(c.name) = LOWER(:category)")
    List<CategoryArticleMapping> findByDateRangeAndCategory(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("category") String category);
}
