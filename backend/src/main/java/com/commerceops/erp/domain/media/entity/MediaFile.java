package com.commerceops.erp.domain.media.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "media_files")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class MediaFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String originalFilename;

    @Column(nullable = false, unique = true, length = 255)
    private String storedFilename;

    @Column(nullable = false, length = 500)
    private String storagePath;

    @Column(nullable = false, length = 500)
    private String publicUrl;

    @Column(nullable = false, length = 100)
    private String contentType;

    @Column(nullable = false)
    private Long size;

    @Column(nullable = false, length = 50)
    private String mediaType;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
