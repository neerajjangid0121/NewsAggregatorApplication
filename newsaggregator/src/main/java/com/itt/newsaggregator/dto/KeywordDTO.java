package com.itt.newsaggregator.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KeywordDTO {
    private String keyId;
    private String keyword;
    private Boolean isRestricted;
}
