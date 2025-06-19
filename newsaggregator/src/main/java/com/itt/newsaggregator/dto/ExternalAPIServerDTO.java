package com.itt.newsaggregator.dto;

import java.time.LocalDateTime;

public class ExternalAPIServerDTO {
    private String id;
    private String serverName;
    private String apiKey;
    private String url;
    private boolean isActive;
    private LocalDateTime lastFetched;
}
