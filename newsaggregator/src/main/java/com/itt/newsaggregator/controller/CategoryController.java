package com.itt.newsaggregator.controller;

import com.itt.newsaggregator.service.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping("/categories")
    public ResponseEntity<Void> addCategory(@RequestBody String name) {
        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        boolean created = categoryService.addCategory(name.trim());

        return created ? ResponseEntity.ok().build() : ResponseEntity.status(HttpStatus.CONFLICT).build();
    }
}