package com.commerceops.erp.domain.media.dto;

import com.commerceops.erp.domain.media.entity.MediaFile;

import java.time.LocalDateTime;

public record MediaFileResponse(
        Long id,
        String originalFilename,
        String storedFilename,
        String url,
        String contentType,
        Long size,
        String mediaType,
        LocalDateTime createdAt
) {
    public static MediaFileResponse from(MediaFile mediaFile) {
        return new MediaFileResponse(
                mediaFile.getId(),
                mediaFile.getOriginalFilename(),
                mediaFile.getStoredFilename(),
                mediaFile.getPublicUrl(),
                mediaFile.getContentType(),
                mediaFile.getSize(),
                mediaFile.getMediaType(),
                mediaFile.getCreatedAt()
        );
    }
}
