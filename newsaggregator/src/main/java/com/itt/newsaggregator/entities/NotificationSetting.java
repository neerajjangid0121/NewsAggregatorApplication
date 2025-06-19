package com.itt.newsaggregator.entities;

import jakarta.persistence.*;

public class NotificationSetting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false)
    @JoinColumn(name="user_id")
    private User user;

    @ManyToOne(optional=false)
    @JoinColumn(name="category_id")
    private Category category;

    @Column(name="is_enabled", nullable=false)
    private boolean enabled = true;
}
