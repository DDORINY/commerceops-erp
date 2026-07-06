package com.commerceops.erp.domain.settings.dto;

import com.commerceops.erp.domain.settings.entity.TermsVersion;
import com.commerceops.erp.domain.settings.enums.TermsType;

import java.time.LocalDateTime;

public record TermsVersionResponse(
        Long id,
        TermsType type,
        String title,
        String content,
        String version,
        LocalDateTime effectiveFrom,
        Boolean active,
        Long createdBy,
        LocalDateTime createdAt
) {
    public static TermsVersionResponse from(TermsVersion termsVersion) {
        return new TermsVersionResponse(
                termsVersion.getId(),
                termsVersion.getType(),
                termsVersion.getTitle(),
                termsVersion.getContent(),
                termsVersion.getVersion(),
                termsVersion.getEffectiveFrom(),
                termsVersion.getActive(),
                termsVersion.getCreatedBy() != null ? termsVersion.getCreatedBy().getId() : null,
                termsVersion.getCreatedAt()
        );
    }
}
