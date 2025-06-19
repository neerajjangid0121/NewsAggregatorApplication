package com.itt.newsaggregator.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor

public class ArticleDTO {

    private String id;
    private String title;
    private LocalDateTime publishedAt;
    private String url;
    private String description;
    private String content;
    private String serverName;

    //private List<String> categories;
    //private List<String> keywords;

}
