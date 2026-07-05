package com.commerceops.erp.domain.banner.repository;

import com.commerceops.erp.domain.banner.entity.MainBanner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MainBannerRepository extends JpaRepository<MainBanner, Long> {

    List<MainBanner> findAllByOrderByPositionAscSortOrderAscIdAsc();

    @Query("""
            select b
            from MainBanner b
            where b.active = true
              and (b.startsAt is null or b.startsAt <= :now)
              and (b.endsAt is null or b.endsAt >= :now)
            """)
    List<MainBanner> findVisibleBanners(@Param("now") LocalDateTime now);
}
