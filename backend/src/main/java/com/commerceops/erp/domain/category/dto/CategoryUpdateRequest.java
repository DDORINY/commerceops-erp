package com.commerceops.erp.domain.category.dto;

public record CategoryUpdateRequest(
        String name,
        Long parentId,
        Integer sortOrder,
        Boolean active,
        Boolean visibleInNav,
        String slug
) {
}
