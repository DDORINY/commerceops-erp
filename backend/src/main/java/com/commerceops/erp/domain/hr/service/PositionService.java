package com.commerceops.erp.domain.hr.service;

import com.commerceops.erp.domain.hr.dto.PositionResponse;
import com.commerceops.erp.domain.hr.repository.PositionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PositionService {

    private final PositionRepository positionRepository;

    public List<PositionResponse> getPositions() {
        return positionRepository.findAllByOrderByLevelAscSortOrderAscIdAsc()
                .stream()
                .map(PositionResponse::from)
                .toList();
    }
}
