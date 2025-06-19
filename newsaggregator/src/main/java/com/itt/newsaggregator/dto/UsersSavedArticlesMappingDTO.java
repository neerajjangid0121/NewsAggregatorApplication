package com.itt.newsaggregator.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsersSavedArticlesMappingDTO {
  @NotBlank
  private String id;

  @NotBlank
  private String userId;

  @NotBlank
  private String savedArticleId;
}
