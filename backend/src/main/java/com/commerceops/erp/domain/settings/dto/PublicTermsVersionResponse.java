package com.commerceops.erp.domain.settings.dto;

import com.commerceops.erp.domain.settings.entity.TermsVersion;
import com.commerceops.erp.domain.settings.enums.TermsType;

import java.time.LocalDateTime;

public record PublicTermsVersionResponse(
        TermsType type,
        String title,
        String content,
        String version,
        LocalDateTime effectiveFrom
) {
    public static PublicTermsVersionResponse from(TermsVersion termsVersion) {
        return new PublicTermsVersionResponse(
                termsVersion.getType(),
                termsVersion.getTitle(),
                termsVersion.getContent(),
                termsVersion.getVersion(),
                termsVersion.getEffectiveFrom()
        );
    }
}
