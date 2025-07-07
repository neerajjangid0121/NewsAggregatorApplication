package com.itt.newsaggregator.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor

public class ArticleDTO {

    private Long id;
    private String title;
    private String publishedAt;
    private String url;
    private String description;
    private String content;
    private String serverName;

    private List<String> categories = new ArrayList<>();
    private Integer reportCount;
    private String status;


    public static ArticleDTO fromJson(JsonNode node) {
        ArticleDTO dto = new ArticleDTO();
        dto.setTitle(node.path("title").asText(""));
        dto.setDescription(node.path("description").asText(""));
        dto.setContent(node.path("content").asText(""));
        dto.setUrl(node.path("url").asText(""));
        dto.setPublishedAt(node.path("published_at").asText(""));
        dto.setServerName(node.path("source").asText(""));

        JsonNode categoryNode = node.path("categories");
        if (categoryNode.isArray()) {
            for (JsonNode c : categoryNode) {
                dto.getCategories().add(c.asText().toLowerCase().trim());
            }
        }

        return dto;
    }

}
