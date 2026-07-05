package com.commerceops.erp.domain.banner.entity;

import com.commerceops.erp.domain.banner.enums.BannerPosition;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "main_banners")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class MainBanner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(length = 200)
    private String subtitle;

    @Column(length = 500)
    private String description;

    @Column(length = 500)
    private String imageUrl;

    @Column(length = 500)
    private String linkUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private BannerPosition position;

    @Builder.Default
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "starts_at")
    private LocalDateTime startsAt;

    @Column(name = "ends_at")
    private LocalDateTime endsAt;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public void update(String title, String subtitle, String description, String imageUrl,
                       String linkUrl, BannerPosition position, Integer sortOrder,
                       Boolean active, LocalDateTime startsAt, LocalDateTime endsAt) {
        if (title != null) {
            this.title = title;
        }
        this.subtitle = subtitle;
        this.description = description;
        this.imageUrl = imageUrl;
        this.linkUrl = linkUrl;
        if (position != null) {
            this.position = position;
        }
        if (sortOrder != null) {
            this.sortOrder = sortOrder;
        }
        if (active != null) {
            this.active = active;
        }
        this.startsAt = startsAt;
        this.endsAt = endsAt;
    }

    public void deactivate() {
        this.active = false;
    }
}
