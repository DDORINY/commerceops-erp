package com.commerceops.erp.domain.settings.dto;

import com.commerceops.erp.domain.settings.entity.BusinessSettings;

import java.time.LocalDateTime;

public record BusinessSettingsResponse(
        Long id,
        String companyName,
        String representativeName,
        String businessRegistrationNumber,
        String mailOrderBusinessNumber,
        String address,
        String customerServicePhone,
        String customerServiceEmail,
        String brandName,
        Long updatedBy,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static BusinessSettingsResponse empty() {
        return new BusinessSettingsResponse(null, null, null, null, null, null, null, null, null, null, null, null);
    }

    public static BusinessSettingsResponse from(BusinessSettings settings) {
        if (settings == null) {
            return empty();
        }
        return new BusinessSettingsResponse(
                settings.getId(),
                settings.getCompanyName(),
                settings.getRepresentativeName(),
                settings.getBusinessRegistrationNumber(),
                settings.getMailOrderBusinessNumber(),
                settings.getAddress(),
                settings.getCustomerServicePhone(),
                settings.getCustomerServiceEmail(),
                settings.getBrandName(),
                settings.getUpdatedBy() != null ? settings.getUpdatedBy().getId() : null,
                settings.getCreatedAt(),
                settings.getUpdatedAt()
        );
    }
}
