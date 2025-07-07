package com.itt.newsaggregator.service;

import com.itt.newsaggregator.entities.Category;
import com.itt.newsaggregator.entities.User;
import com.itt.newsaggregator.repository.CategoryRepository;
import com.itt.newsaggregator.repository.UserRepository;
import com.itt.newsaggregator.dto.CategoryDTO;
import com.itt.newsaggregator.exception.CategoryNotFoundException;
import com.itt.newsaggregator.exception.UserNotFoundException;
import com.itt.newsaggregator.exception.CategoryAlreadyExistsException;
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
            throw new CategoryAlreadyExistsException("Category '" + name + "' already exists");
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
                .orElseThrow(() -> new CategoryNotFoundException("Category with ID " + categoryId + " not found"));
        User admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new UserNotFoundException("Admin with ID " + adminUserId + " not found"));

        category.setIsRestricted(true);
        category.setRestrictedBy(admin.getId());
        category.setRestrictedAt(LocalDateTime.now());
        category.setRestrictionReason(reason);

        categoryRepository.save(category);
    }

    @Transactional
    public void unrestrictCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException("Category with ID " + categoryId + " not found"));

        category.setIsRestricted(false);
        category.setRestrictedBy(null);
        category.setRestrictedAt(null);
        category.setRestrictionReason(null);

        categoryRepository.save(category);
    }

    public List<CategoryDTO> getRestrictedCategories() {
        return categoryRepository.findByIsRestrictedTrue().stream()
                .map(cat -> new CategoryDTO(cat.getCategoryId(), cat.getName(), cat.getIsRestricted()))
                .toList();
    }

    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(cat -> new CategoryDTO(cat.getCategoryId(), cat.getName(), cat.getIsRestricted()))
                .toList();
    }

    public boolean isCategoryRestricted(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException("Category with ID " + categoryId + " not found"));
        return category.getIsRestricted();
    }
}
