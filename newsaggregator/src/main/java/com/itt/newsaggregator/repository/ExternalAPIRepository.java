package com.itt.newsaggregator.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.itt.newsaggregator.entities.ExternalAPIDetails;

public interface ExternalAPIRepository extends JpaRepository<ExternalAPIDetails, String>{
    List<ExternalAPIDetails> findByIsActiveTrue();
    Optional<ExternalAPIDetails> findByServerName(String serverName);
}
