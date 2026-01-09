package com.itt.newsaggregator.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationSettingsDTO {
    private Long userId;
    private Map<String, Boolean> categorySettings; // category name -> enabled
    private List<String> keywords;
}