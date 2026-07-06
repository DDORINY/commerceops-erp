package com.commerceops.erp.domain.settings.dto;

import com.commerceops.erp.domain.settings.entity.BusinessSettings;

public record PublicBusinessSettingsResponse(
        String companyName,
        String representativeName,
        String businessRegistrationNumber,
        String mailOrderBusinessNumber,
        String address,
        String customerServicePhone,
        String customerServiceEmail,
        String brandName
) {
    public static PublicBusinessSettingsResponse empty() {
        return new PublicBusinessSettingsResponse(null, null, null, null, null, null, null, null);
    }

    public static PublicBusinessSettingsResponse from(BusinessSettings settings) {
        if (settings == null) {
            return empty();
        }
        return new PublicBusinessSettingsResponse(
                settings.getCompanyName(),
                settings.getRepresentativeName(),
                settings.getBusinessRegistrationNumber(),
                settings.getMailOrderBusinessNumber(),
                settings.getAddress(),
                settings.getCustomerServicePhone(),
                settings.getCustomerServiceEmail(),
                settings.getBrandName()
        );
    }
}
