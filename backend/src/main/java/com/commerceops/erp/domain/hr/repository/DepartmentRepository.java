package com.commerceops.erp.domain.hr.repository;

import com.commerceops.erp.domain.hr.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    List<Department> findAllByOrderBySortOrderAscIdAsc();

    List<Department> findByActiveTrueOrderBySortOrderAscIdAsc();
}
