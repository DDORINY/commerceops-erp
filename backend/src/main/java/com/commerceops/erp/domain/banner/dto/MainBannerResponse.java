package com.commerceops.erp.domain.banner.dto;

import com.commerceops.erp.domain.banner.entity.MainBanner;
import com.commerceops.erp.domain.banner.enums.BannerPosition;

import java.time.LocalDateTime;

public record MainBannerResponse(
        Long id,
        String title,
        String subtitle,
        String description,
        String imageUrl,
        String linkUrl,
        BannerPosition position,
        Integer sortOrder,
        Boolean active,
        LocalDateTime startsAt,
        LocalDateTime endsAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static MainBannerResponse from(MainBanner banner) {
        return new MainBannerResponse(
                banner.getId(),
                banner.getTitle(),
                banner.getSubtitle(),
                banner.getDescription(),
                banner.getImageUrl(),
                banner.getLinkUrl(),
                banner.getPosition(),
                banner.getSortOrder(),
                banner.getActive(),
                banner.getStartsAt(),
                banner.getEndsAt(),
                banner.getCreatedAt(),
                banner.getUpdatedAt()
        );
    }
}
