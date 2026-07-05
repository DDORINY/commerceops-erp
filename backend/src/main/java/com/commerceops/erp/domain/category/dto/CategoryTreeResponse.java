package com.commerceops.erp.domain.category.dto;

import com.commerceops.erp.domain.category.entity.Category;

import java.util.List;

public record CategoryTreeResponse(
        Long id,
        String name,
        Long parentId,
        Integer depth,
        Integer sortOrder,
        Boolean active,
        Boolean visibleInNav,
        String slug,
        List<CategoryTreeResponse> children
) {
    public static CategoryTreeResponse from(Category category, List<CategoryTreeResponse> children) {
        return new CategoryTreeResponse(
                category.getId(),
                category.getName(),
                category.getParent() != null ? category.getParent().getId() : null,
                category.getDepth(),
                category.getSortOrder(),
                category.getActive(),
                category.getVisibleInNav(),
                category.getSlug(),
                children
        );
    }
}
