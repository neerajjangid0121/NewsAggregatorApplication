package com.itt.newsaggregator.interfaces;

import java.util.List;

import com.itt.newsaggregator.entities.Article;

public interface NewsAPIClient {
    List<Article> fetchNews();
}
