package com.commerceops.erp.domain.media.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "commerceops.media")
public record MediaStorageProperties(
        String uploadDir,
        String publicBaseUrl,
        String publicPath,
        long maxFileSize,
        List<String> allowedContentTypes,
        List<String> allowedExtensions
) {
    public MediaStorageProperties {
        if (uploadDir == null || uploadDir.isBlank()) {
            uploadDir = "uploads";
        }
        if (publicBaseUrl == null || publicBaseUrl.isBlank()) {
            publicBaseUrl = "http://localhost:8080";
        }
        if (publicPath == null || publicPath.isBlank()) {
            publicPath = "/uploads";
        }
        if (maxFileSize <= 0) {
            maxFileSize = 5L * 1024L * 1024L;
        }
        if (allowedContentTypes == null || allowedContentTypes.isEmpty()) {
            allowedContentTypes = List.of("image/jpeg", "image/png", "image/webp", "image/gif");
        }
        if (allowedExtensions == null || allowedExtensions.isEmpty()) {
            allowedExtensions = List.of("jpg", "jpeg", "png", "webp", "gif");
        }
    }
}
