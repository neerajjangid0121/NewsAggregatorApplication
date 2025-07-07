package com.itt.newsaggregator.service;

import com.itt.newsaggregator.entities.User;
import com.itt.newsaggregator.entities.Article;
import com.itt.newsaggregator.entities.UserArticleReadHistory;
import com.itt.newsaggregator.repository.UserArticleReadHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserArticleReadHistoryService {
    @Autowired
    private UserArticleReadHistoryRepository readHistoryRepository;

    public void recordArticleRead(User user, Article article) {
        UserArticleReadHistory history = new UserArticleReadHistory();
        history.setUser(user);
        history.setArticle(article);
        history.setReadAt(LocalDateTime.now());
        readHistoryRepository.save(history);
    }

    public List<UserArticleReadHistory> getReadHistoryForUser(User user) {
        return readHistoryRepository.findByUser(user);
    }

    public boolean hasUserReadArticle(User user, Article article) {
        return !readHistoryRepository.findByUserAndArticle(user, article).isEmpty();
    }
}