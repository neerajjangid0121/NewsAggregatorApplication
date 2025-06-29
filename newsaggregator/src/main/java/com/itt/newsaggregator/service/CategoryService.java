package com.itt.newsaggregator.service;

import com.itt.newsaggregator.entities.Category;
import com.itt.newsaggregator.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
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
}
