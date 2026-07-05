package com.commerceops.erp.domain.category.service;

import com.commerceops.erp.domain.category.dto.CategoryCreateRequest;
import com.commerceops.erp.domain.category.dto.CategoryResponse;
import com.commerceops.erp.domain.category.dto.CategoryTreeResponse;
import com.commerceops.erp.domain.category.dto.CategoryUpdateRequest;
import com.commerceops.erp.domain.category.entity.Category;
import com.commerceops.erp.domain.category.repository.CategoryRepository;
import com.commerceops.erp.global.exception.BusinessException;
import com.commerceops.erp.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findByActiveTrueOrderByDepthAscSortOrderAscIdAsc()
                .stream()
                .map(CategoryResponse::from)
                .toList();
    }

    public List<CategoryTreeResponse> getNavigationCategories() {
        return buildTree(categoryRepository.findByActiveTrueAndVisibleInNavTrueOrderByDepthAscSortOrderAscIdAsc());
    }

    public List<CategoryTreeResponse> getAdminCategoryTree() {
        return buildTree(categoryRepository.findAllByOrderByDepthAscSortOrderAscIdAsc());
    }

    @Transactional
    public CategoryResponse createCategory(CategoryCreateRequest request) {
        String name = normalizeName(request.name());
        Category parent = resolveParent(request.parentId());
        String slug = normalizeSlug(request.slug());
        validateSlug(slug, null);

        Category category = Category.builder()
                .name(name)
                .parent(parent)
                .depth(parent != null ? parent.getDepth() + 1 : 0)
                .sortOrder(request.sortOrder() != null ? request.sortOrder() : 0)
                .active(request.active() == null || request.active())
                .visibleInNav(request.visibleInNav() == null || request.visibleInNav())
                .slug(slug)
                .build();
        return CategoryResponse.from(categoryRepository.save(category));
    }

    @Transactional
    public CategoryResponse updateCategory(Long categoryId, CategoryUpdateRequest request) {
        Category category = getCategoryById(categoryId);
        String name = request.name() != null ? normalizeName(request.name()) : category.getName();
        Category parent = resolveParent(request.parentId());

        if (parent != null && parent.getId().equals(categoryId)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (parent != null && createsCycle(categoryId, parent)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        String slug = request.slug() != null ? normalizeSlug(request.slug()) : category.getSlug();
        validateSlug(slug, categoryId);

        category.update(
                name,
                parent,
                request.sortOrder(),
                request.active(),
                request.visibleInNav(),
                slug
        );
        return CategoryResponse.from(category);
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));
    }

    private List<CategoryTreeResponse> buildTree(List<Category> categories) {
        Map<Long, List<Category>> childrenByParent = new HashMap<>();
        List<Category> roots = new ArrayList<>();

        for (Category category : categories) {
            Long parentId = category.getParent() != null ? category.getParent().getId() : null;
            if (parentId == null) {
                roots.add(category);
            } else {
                childrenByParent.computeIfAbsent(parentId, key -> new ArrayList<>()).add(category);
            }
        }

        Comparator<Category> sort = Comparator
                .comparing(Category::getSortOrder)
                .thenComparing(Category::getId);
        roots.sort(sort);
        childrenByParent.values().forEach(children -> children.sort(sort));

        return roots.stream()
                .map(root -> toTreeResponse(root, childrenByParent))
                .toList();
    }

    private CategoryTreeResponse toTreeResponse(Category category, Map<Long, List<Category>> childrenByParent) {
        List<CategoryTreeResponse> children = childrenByParent
                .getOrDefault(category.getId(), List.of())
                .stream()
                .map(child -> toTreeResponse(child, childrenByParent))
                .toList();
        return CategoryTreeResponse.from(category, children);
    }

    private Category resolveParent(Long parentId) {
        if (parentId == null) {
            return null;
        }
        return categoryRepository.findById(parentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));
    }

    private boolean createsCycle(Long categoryId, Category parent) {
        Category current = parent;
        while (current != null) {
            if (current.getId().equals(categoryId)) {
                return true;
            }
            current = current.getParent();
        }
        return false;
    }

    private String normalizeName(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        return name.trim();
    }

    private String normalizeSlug(String slug) {
        if (slug == null) {
            return null;
        }
        String trimmed = slug.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void validateSlug(String slug, Long categoryId) {
        if (slug == null) {
            return;
        }
        boolean duplicated = categoryId == null
                ? categoryRepository.existsBySlug(slug)
                : categoryRepository.existsBySlugAndIdNot(slug, categoryId);
        if (duplicated) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }
}
