package com.commerceops.erp.domain.warehouse.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record WarehouseCreateRequest(
        @NotBlank
        @Size(max = 30)
        @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "창고 코드는 영문, 숫자, _, -만 사용할 수 있습니다.")
        String code,
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 300) String address
) {}
