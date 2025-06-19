package com.itt.newsaggregator.repository;

import com.itt.newsaggregator.entities.CategoryArticleMapping;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryArticleMappingRepository extends JpaRepository<CategoryArticleMapping, Long> {
}
