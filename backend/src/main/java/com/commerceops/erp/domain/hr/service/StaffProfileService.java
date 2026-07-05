package com.commerceops.erp.domain.hr.service;

import com.commerceops.erp.domain.hr.dto.StaffProfileResponse;
import com.commerceops.erp.domain.hr.repository.StaffProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StaffProfileService {

    private final StaffProfileRepository staffProfileRepository;

    public List<StaffProfileResponse> getStaffProfiles() {
        return staffProfileRepository.findAllByOrderByIdAsc()
                .stream()
                .map(StaffProfileResponse::from)
                .toList();
    }
}
