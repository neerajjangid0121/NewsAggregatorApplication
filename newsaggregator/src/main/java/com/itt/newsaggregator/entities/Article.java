package com.itt.newsaggregator.entities;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static jakarta.persistence.CascadeType.ALL;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Articles")
public class Article {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(nullable = false, unique = true)
    private String url;

    @Column(nullable = false,length = 2000)
    private String description;

    @Column(nullable = false)
    private String content;

    @ManyToOne
    @JoinColumn(name = "server_id", nullable = false)
    private ExternalAPIDetails externalAPIDetails;

}
