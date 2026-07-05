package com.commerceops.erp.domain.banner.dto;

import com.commerceops.erp.domain.banner.enums.BannerPosition;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public record MainBannerRequest(
        @NotBlank(message = "배너 제목은 필수입니다.")
        String title,
        String subtitle,
        String description,
        String imageUrl,
        String linkUrl,
        BannerPosition position,
        Integer sortOrder,
        Boolean active,
        LocalDateTime startsAt,
        LocalDateTime endsAt
) {
}
