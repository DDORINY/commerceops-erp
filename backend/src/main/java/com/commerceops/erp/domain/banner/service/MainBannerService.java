package com.commerceops.erp.domain.banner.service;

import com.commerceops.erp.domain.banner.dto.MainBannerRequest;
import com.commerceops.erp.domain.banner.dto.MainBannerResponse;
import com.commerceops.erp.domain.banner.entity.MainBanner;
import com.commerceops.erp.domain.banner.enums.BannerPosition;
import com.commerceops.erp.domain.banner.repository.MainBannerRepository;
import com.commerceops.erp.global.exception.BusinessException;
import com.commerceops.erp.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MainBannerService {

    private final MainBannerRepository mainBannerRepository;

    public List<MainBannerResponse> getVisibleBanners() {
        return sortBanners(mainBannerRepository.findVisibleBanners(LocalDateTime.now()))
                .stream()
                .map(MainBannerResponse::from)
                .toList();
    }

    public List<MainBannerResponse> getAdminBanners() {
        return sortBanners(mainBannerRepository.findAllByOrderByPositionAscSortOrderAscIdAsc())
                .stream()
                .map(MainBannerResponse::from)
                .toList();
    }

    public MainBannerResponse getAdminBanner(Long bannerId) {
        return MainBannerResponse.from(getBannerById(bannerId));
    }

    @Transactional
    public MainBannerResponse createBanner(MainBannerRequest request) {
        validateRequest(request);
        MainBanner banner = MainBanner.builder()
                .title(request.title().trim())
                .subtitle(normalize(request.subtitle()))
                .description(normalize(request.description()))
                .imageUrl(normalize(request.imageUrl()))
                .linkUrl(normalize(request.linkUrl()))
                .position(request.position() != null ? request.position() : BannerPosition.MAIN_TOP)
                .sortOrder(request.sortOrder() != null ? request.sortOrder() : 0)
                .active(request.active() == null || request.active())
                .startsAt(request.startsAt())
                .endsAt(request.endsAt())
                .build();
        return MainBannerResponse.from(mainBannerRepository.save(banner));
    }

    @Transactional
    public MainBannerResponse updateBanner(Long bannerId, MainBannerRequest request) {
        validateRequest(request);
        MainBanner banner = getBannerById(bannerId);
        banner.update(
                request.title().trim(),
                normalize(request.subtitle()),
                normalize(request.description()),
                normalize(request.imageUrl()),
                normalize(request.linkUrl()),
                request.position() != null ? request.position() : BannerPosition.MAIN_TOP,
                request.sortOrder() != null ? request.sortOrder() : 0,
                request.active() == null || request.active(),
                request.startsAt(),
                request.endsAt()
        );
        return MainBannerResponse.from(banner);
    }

    @Transactional
    public void deactivateBanner(Long bannerId) {
        getBannerById(bannerId).deactivate();
    }

    private MainBanner getBannerById(Long bannerId) {
        return mainBannerRepository.findById(bannerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
    }

    private void validateRequest(MainBannerRequest request) {
        if (request.title() == null || request.title().isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (request.startsAt() != null && request.endsAt() != null
                && request.startsAt().isAfter(request.endsAt())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private List<MainBanner> sortBanners(List<MainBanner> banners) {
        return banners.stream()
                .sorted(Comparator
                        .comparing((MainBanner banner) -> banner.getPosition().ordinal())
                        .thenComparing(MainBanner::getSortOrder)
                        .thenComparing(MainBanner::getId))
                .toList();
    }
}
