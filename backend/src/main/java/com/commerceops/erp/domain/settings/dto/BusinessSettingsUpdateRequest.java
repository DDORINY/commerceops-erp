package com.commerceops.erp.domain.settings.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record BusinessSettingsUpdateRequest(
        @Size(max = 120) String companyName,
        @Size(max = 80) String representativeName,
        @Pattern(regexp = "^[0-9-]{0,30}$", message = "사업자등록번호는 숫자와 하이픈만 입력할 수 있습니다.")
        String businessRegistrationNumber,
        @Size(max = 60) String mailOrderBusinessNumber,
        @Size(max = 500) String address,
        @Pattern(regexp = "^[0-9+\\-()\\s]{0,30}$", message = "전화번호 형식이 올바르지 않습니다.")
        String customerServicePhone,
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        @Size(max = 120)
        String customerServiceEmail,
        @Size(max = 120) String brandName
) {
}
