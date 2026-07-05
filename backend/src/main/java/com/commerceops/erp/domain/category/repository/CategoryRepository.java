package com.commerceops.erp.domain.category.repository;

import com.commerceops.erp.domain.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByName(String name);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);

    List<Category> findAllByOrderByDepthAscSortOrderAscIdAsc();

    List<Category> findByActiveTrueOrderByDepthAscSortOrderAscIdAsc();

    List<Category> findByActiveTrueAndVisibleInNavTrueOrderByDepthAscSortOrderAscIdAsc();
}
