package com.commerceops.erp.domain.hr.repository;

import com.commerceops.erp.domain.hr.entity.Position;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PositionRepository extends JpaRepository<Position, Long> {

    List<Position> findAllByOrderByLevelAscSortOrderAscIdAsc();

    List<Position> findByActiveTrueOrderByLevelAscSortOrderAscIdAsc();
}
