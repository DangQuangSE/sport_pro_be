package com.sport_pro_be.category.service;

import com.sport_pro_be.category.domain.Category;
import com.sport_pro_be.category.dto.CategoryRequest;
import com.sport_pro_be.category.dto.CategoryResponse;
import com.sport_pro_be.category.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    void createCategory_whenSlugExists_shouldAppendSuffix() {
        CategoryRequest request = new CategoryRequest("Nike", null, null, null, null);

        when(categoryRepository.existsByNameIgnoreCase("Nike")).thenReturn(false);
        when(categoryRepository.existsBySlug("nike")).thenReturn(true);
        when(categoryRepository.existsBySlug("nike-1")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CategoryResponse response = categoryService.createCategory(request);

        assertEquals("nike-1", response.getSlug());
        ArgumentCaptor<Category> categoryCaptor = ArgumentCaptor.forClass(Category.class);
        verify(categoryRepository).save(categoryCaptor.capture());
        assertEquals("nike-1", categoryCaptor.getValue().getSlug());
    }

    @Test
    void getCategoryTree_shouldBuildHierarchy() {
        Category root = new Category();
        root.setId(1L);
        root.setName("Root");
        root.setSlug("root");
        root.setDisplayOrder(0);

        Category child = new Category();
        child.setId(2L);
        child.setName("Child");
        child.setSlug("child");
        child.setParentId(1L);
        child.setDisplayOrder(0);

        Category anotherRoot = new Category();
        anotherRoot.setId(3L);
        anotherRoot.setName("Root2");
        anotherRoot.setSlug("root-2");
        anotherRoot.setDisplayOrder(1);

        when(categoryRepository.findAllByIsActiveTrueOrderByDisplayOrderAsc())
                .thenReturn(List.of(root, child, anotherRoot));

        List<CategoryResponse> result = categoryService.getCategoryTree();

        assertEquals(2, result.size());
        CategoryResponse firstRoot = result.stream().filter(item -> item.getId().equals(1L)).findFirst().orElse(null);
        assertNotNull(firstRoot);
        assertEquals(1, firstRoot.getChildren().size());
        assertEquals(2L, firstRoot.getChildren().getFirst().getId());
    }
}
