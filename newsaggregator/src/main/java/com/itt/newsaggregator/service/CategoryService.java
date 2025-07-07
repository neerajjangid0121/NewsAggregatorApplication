package com.itt.newsaggregator.service;

import com.itt.newsaggregator.entities.Category;
import com.itt.newsaggregator.entities.User;
import com.itt.newsaggregator.repository.CategoryRepository;
import com.itt.newsaggregator.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public CategoryService(CategoryRepository categoryRepository, UserRepository userRepository) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    public boolean addCategory(String name) {

        name = name.trim().replaceAll("^\"|\"$", "");

        if (categoryRepository.findByNameIgnoreCase(name).isPresent()) {
            return false;
        }

        Category category = new Category();
        category.setName(name);
        categoryRepository.save(category);
        return true;
    }

    // Category Restriction Methods

    @Transactional
    public void restrictCategory(Long categoryId, Long adminUserId, String reason) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        User admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        category.setIsRestricted(true);
        category.setRestrictedBy(admin.getId());
        category.setRestrictedAt(LocalDateTime.now());
        category.setRestrictionReason(reason);

        categoryRepository.save(category);
    }

    @Transactional
    public void unrestrictCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        category.setIsRestricted(false);
        category.setRestrictedBy(null);
        category.setRestrictedAt(null);
        category.setRestrictionReason(null);

        categoryRepository.save(category);
    }

    public List<Category> getRestrictedCategories() {
        return categoryRepository.findByIsRestrictedTrue();
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public boolean isCategoryRestricted(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        return category.getIsRestricted();
    }
}
