package com.itt.newsaggregator.controller;

import com.itt.newsaggregator.service.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/categories/{categoryId}/restrict")
    public ResponseEntity<Void> restrictCategory(
            @PathVariable Long categoryId,
            @RequestParam Long adminUserId,
            @RequestParam String reason) {
        categoryService.restrictCategory(categoryId, adminUserId, reason);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/categories/{categoryId}/restrict")
    public ResponseEntity<Void> removeCategoryRestriction(@PathVariable Long categoryId) {
        categoryService.unrestrictCategory(categoryId);
        return ResponseEntity.ok().build();
    }


    @GetMapping("/categories")
    public ResponseEntity<?> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @GetMapping("/categories/restricted")
    public ResponseEntity<?> getRestrictedCategories() {
        return ResponseEntity.ok(categoryService.getRestrictedCategories());
    }
}