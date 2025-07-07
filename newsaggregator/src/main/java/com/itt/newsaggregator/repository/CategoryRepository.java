package com.itt.newsaggregator.repository;

import com.itt.newsaggregator.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByNameIgnoreCase(String name);

    // Moderation methods
    List<Category> findByIsRestrictedTrue();
}
