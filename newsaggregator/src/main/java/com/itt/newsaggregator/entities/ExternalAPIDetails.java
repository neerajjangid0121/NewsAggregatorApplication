package com.itt.newsaggregator.entities;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ExternalAPIDetails")

public class ExternalAPIDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String apiKey;
    @Column(nullable = false)
    private String serverName;
    @Column(nullable = false)
    private String url;
    @Column(nullable = false)
    private boolean isActive;
    @Column(name = "lastFetched")
    private LocalDateTime lastFetched;
}
