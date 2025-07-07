package com.itt.newsaggregator.service;

import com.itt.newsaggregator.entities.Keyword;
import com.itt.newsaggregator.entities.User;
import com.itt.newsaggregator.repository.KeywordRepository;
import com.itt.newsaggregator.repository.UserRepository;
import com.itt.newsaggregator.dto.KeywordDTO;
import com.itt.newsaggregator.exception.KeywordNotFoundException;
import com.itt.newsaggregator.exception.UserNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class KeywordService {

    private final KeywordRepository keywordRepository;
    private final UserRepository userRepository;

    public KeywordService(KeywordRepository keywordRepository, UserRepository userRepository) {
        this.keywordRepository = keywordRepository;
        this.userRepository = userRepository;
    }

    // Keyword Restriction Methods

    @Transactional
    public void restrictKeyword(String keywordText, Long adminUserId, String reason) {
        User admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new UserNotFoundException("Admin with ID " + adminUserId + " not found"));

        // Find or create keyword
        Keyword keyword = keywordRepository.findByKeyword(keywordText)
                .orElseGet(() -> {
                    Keyword newKeyword = new Keyword();
                    newKeyword.setKeyword(keywordText);
                    return newKeyword;
                });

        keyword.setIsRestricted(true);
        keyword.setRestrictedBy(admin.getId());
        keyword.setRestrictedAt(LocalDateTime.now());
        keyword.setRestrictionReason(reason);

        keywordRepository.save(keyword);
    }

    @Transactional
    public void unrestrictKeyword(String keywordText) {
        Keyword keyword = keywordRepository.findByKeyword(keywordText)
                .orElseThrow(() -> new KeywordNotFoundException("Keyword '" + keywordText + "' not found"));

        keyword.setIsRestricted(false);
        keyword.setRestrictedBy(null);
        keyword.setRestrictedAt(null);
        keyword.setRestrictionReason(null);

        keywordRepository.save(keyword);
    }

    public List<KeywordDTO> getRestrictedKeywords() {
        return keywordRepository.findByIsRestrictedTrue().stream()
                .map(k -> new KeywordDTO(String.valueOf(k.getKeyId()), k.getKeyword(), k.getIsRestricted()))
                .toList();
    }

    public List<KeywordDTO> getAllKeywords() {
        return keywordRepository.findAll().stream()
                .map(k -> new KeywordDTO(String.valueOf(k.getKeyId()), k.getKeyword(), k.getIsRestricted()))
                .toList();
    }

    public boolean isKeywordRestricted(String keywordText) {
        return keywordRepository.findByKeyword(keywordText)
                .map(Keyword::getIsRestricted)
                .orElse(false);
    }

    // Check if any restricted keywords match article content
    public boolean hasRestrictedKeywords(String title, String description, String content) {
        List<KeywordDTO> restrictedKeywords = getRestrictedKeywords();

        String lowerTitle = title != null ? title.toLowerCase() : "";
        String lowerDescription = description != null ? description.toLowerCase() : "";
        String lowerContent = content != null ? content.toLowerCase() : "";

        for (KeywordDTO keyword : restrictedKeywords) {
            String keywordText = keyword.getKeyword().toLowerCase();
            if (lowerTitle.contains(keywordText) ||
                    lowerDescription.contains(keywordText) ||
                    lowerContent.contains(keywordText)) {
                return true;
            }
        }

        return false;
    }
}