package com.itt.newsaggregator.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Keywords")
public class Keyword {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long keyId;

    @Column(nullable = false, unique = true)
    private String keyword;

    @Column(name = "is_restricted", nullable = false)
    private Boolean isRestricted = false;

    @Column(name = "restricted_by")
    private Long restrictedBy;

    @Column(name = "restricted_at")
    private LocalDateTime restrictedAt;

    @Column(name = "restriction_reason", length = 500)
    private String restrictionReason;
}
