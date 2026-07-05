package com.commerceops.erp.domain.hr.service;

import com.commerceops.erp.domain.hr.dto.DepartmentResponse;
import com.commerceops.erp.domain.hr.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    public List<DepartmentResponse> getDepartments() {
        return departmentRepository.findAllByOrderBySortOrderAscIdAsc()
                .stream()
                .map(DepartmentResponse::from)
                .toList();
    }
}
