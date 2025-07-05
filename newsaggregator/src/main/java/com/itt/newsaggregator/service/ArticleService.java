package com.itt.newsaggregator.service;

import com.itt.newsaggregator.dto.ArticleDTO;
import com.itt.newsaggregator.entities.Article;
import com.itt.newsaggregator.entities.CategoryArticleMapping;
import com.itt.newsaggregator.mapper.ArticleMapper;
import com.itt.newsaggregator.repository.CategoryArticleMappingRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ArticleService {

    private final CategoryArticleMappingRepository mappingRepo;
    private final ArticleMapper articleMapper;


    public ArticleService(CategoryArticleMappingRepository mappingRepo, ArticleMapper articleMapper) {
        this.mappingRepo = mappingRepo;
        this.articleMapper = articleMapper;
    }

    public List<ArticleDTO> getArticlesBetweenDates(LocalDate start, LocalDate end, String category) {
        LocalDateTime startTime = start.atStartOfDay();
        LocalDateTime endTime = end.plusDays(1).atStartOfDay();

        List<CategoryArticleMapping> mappings = (category == null || category.isBlank())
                ? mappingRepo.findByDateRange(startTime, endTime)
                : mappingRepo.findByDateRangeAndCategory(startTime, endTime, category);

        Map<Long, ArticleDTO> articleMap = new LinkedHashMap<>();

        for (CategoryArticleMapping cam : mappings) {
            Article article = cam.getArticle();
            Long articleId = article.getId();

            articleMap.putIfAbsent(articleId, articleMapper.toDto(article));
            ArticleDTO dto = articleMap.get(articleId);
            dto.getCategories().add(cam.getCategory().getName());
        }

        return new ArrayList<>(articleMap.values());
    }
}
