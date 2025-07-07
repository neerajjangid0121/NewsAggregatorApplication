package com.itt.newsaggregator.repository;

import com.itt.newsaggregator.entities.Keyword;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface KeywordRepository extends JpaRepository<Keyword, Long> {
    Optional<Keyword> findByKeyword(String keyword);

    // Moderation methods
    List<Keyword> findByIsRestrictedTrue();
}