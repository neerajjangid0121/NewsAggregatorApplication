package com.itt.newsaggregator.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.itt.newsaggregator.entities.Article;
import com.itt.newsaggregator.interfaces.NewsSyncService;
import com.itt.newsaggregator.repository.ArticleRepository;
import com.itt.newsaggregator.interfaces.NewsAPIClient;

@Service
public class NewsSyncServiceImpl implements NewsSyncService{
    private final List<NewsAPIClient> newsClients;
    private final ArticleRepository articleRepository;

    public NewsSyncServiceImpl(List<NewsAPIClient> newsClients, ArticleRepository articleRepository) {
        this.newsClients = newsClients;
        this.articleRepository = articleRepository;
    }
    @Override
    public void syncAllFeeds() {
        for (NewsAPIClient client : newsClients) {
            try {
                List<Article> articles = client.fetchNews();
                articleRepository.saveAll(articles);
            } catch (Exception e) {
                System.err.println("Error while syncing from client: " + client.getClass().getSimpleName());
                e.printStackTrace();
            }
        }
    }
}
