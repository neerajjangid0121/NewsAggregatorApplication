package com.itt.newsaggregator.service;

import com.itt.newsaggregator.dto.CategoryDTO;
import com.itt.newsaggregator.entities.Category;
import com.itt.newsaggregator.entities.User;
import com.itt.newsaggregator.exception.CategoryAlreadyExistsException;
import com.itt.newsaggregator.exception.CategoryNotFoundException;
import com.itt.newsaggregator.repository.CategoryRepository;
import com.itt.newsaggregator.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

class CategoryServiceTest {
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private UserRepository userRepository;
    private CategoryService categoryService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        categoryService = new CategoryService(categoryRepository, userRepository);
    }

    @Test
    @DisplayName("addCategory saves new category if not exists")
    void addCategory_SavesIfNotExists() {
        Mockito.when(categoryRepository.findByNameIgnoreCase(anyString())).thenReturn(Optional.empty());
        Mockito.when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));
        boolean result = categoryService.addCategory("Tech");
        assertTrue(result);
    }

    @Test
    @DisplayName("addCategory throws if category exists")
    void addCategory_ThrowsIfExists() {
        Mockito.when(categoryRepository.findByNameIgnoreCase(anyString())).thenReturn(Optional.of(new Category()));
        assertThrows(CategoryAlreadyExistsException.class, () -> categoryService.addCategory("Tech"));
    }

    @Test
    @DisplayName("restrictCategory sets restriction fields")
    void restrictCategory_SetsFields() {
        Category category = new Category();
        category.setCategoryId(1L);
        User admin = new User();
        admin.setId(2L);
        Mockito.when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(admin));
        Mockito.when(categoryRepository.save(any(Category.class))).thenReturn(category);
        categoryService.restrictCategory(1L, 2L, "reason");
        assertTrue(category.getIsRestricted());
        assertEquals(admin.getId(), category.getRestrictedBy());
        assertEquals("reason", category.getRestrictionReason());
    }

    @Test
    @DisplayName("restrictCategory throws if not found")
    void restrictCategory_ThrowsIfNotFound() {
        Mockito.when(categoryRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(CategoryNotFoundException.class, () -> categoryService.restrictCategory(1L, 2L, "reason"));
    }

    @Test
    @DisplayName("unrestrictCategory clears restriction fields")
    void unrestrictCategory_ClearsFields() {
        Category category = new Category();
        category.setCategoryId(1L);
        category.setIsRestricted(true);
        category.setRestrictedBy(2L);
        category.setRestrictionReason("reason");
        Mockito.when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        Mockito.when(categoryRepository.save(any(Category.class))).thenReturn(category);
        categoryService.unrestrictCategory(1L);
        assertFalse(category.getIsRestricted());
        assertNull(category.getRestrictedBy());
        assertNull(category.getRestrictionReason());
    }

    @Test
    @DisplayName("getRestrictedCategories returns DTO list")
    void getRestrictedCategories_ReturnsList() {
        Category category = new Category();
        category.setCategoryId(1L);
        category.setName("Tech");
        category.setIsRestricted(true);
        Mockito.when(categoryRepository.findByIsRestrictedTrue()).thenReturn(List.of(category));
        List<CategoryDTO> result = categoryService.getRestrictedCategories();
        assertEquals(1, result.size());
        assertEquals("Tech", result.get(0).getName());
        assertTrue(result.get(0).getIsRestricted());
    }

    @Test
    @DisplayName("getAllCategories returns DTO list")
    void getAllCategories_ReturnsList() {
        Category category = new Category();
        category.setCategoryId(1L);
        category.setName("Tech");
        category.setIsRestricted(false);
        Mockito.when(categoryRepository.findAll()).thenReturn(List.of(category));
        List<CategoryDTO> result = categoryService.getAllCategories();
        assertEquals(1, result.size());
        assertEquals("Tech", result.get(0).getName());
        assertFalse(result.get(0).getIsRestricted());
    }

    @Test
    @DisplayName("isCategoryRestricted returns true if restricted")
    void isCategoryRestricted_ReturnsTrue() {
        Category category = new Category();
        category.setIsRestricted(true);
        Mockito.when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(category));
        assertTrue(categoryService.isCategoryRestricted(1L));
    }

    @Test
    @DisplayName("isCategoryRestricted throws if not found")
    void isCategoryRestricted_ThrowsIfNotFound() {
        Mockito.when(categoryRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(CategoryNotFoundException.class, () -> categoryService.isCategoryRestricted(1L));
    }
}
