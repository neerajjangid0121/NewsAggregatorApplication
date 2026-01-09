package com.itt.newsaggregator.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UsersKeywordsMappingDTO {
    private String userId;
    private List<String> keywordIds;
    private boolean isEnable;
}