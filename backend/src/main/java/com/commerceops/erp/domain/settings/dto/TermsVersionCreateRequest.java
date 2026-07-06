package com.commerceops.erp.domain.settings.dto;

import com.commerceops.erp.domain.settings.enums.TermsType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record TermsVersionCreateRequest(
        @NotNull TermsType type,
        @NotBlank @Size(max = 160) String title,
        @NotBlank @Size(max = 20000) String content,
        @Size(max = 40) String version,
        LocalDateTime effectiveFrom
) {
}
