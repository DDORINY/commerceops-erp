package com.commerceops.erp.domain.media.service;

import com.commerceops.erp.domain.media.config.MediaStorageProperties;
import com.commerceops.erp.domain.media.repository.MediaFileRepository;
import com.commerceops.erp.global.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class MediaStorageServiceTest {
    @TempDir
    Path tempDir;

    private MediaStorageService service() {
        MediaStorageProperties properties = new MediaStorageProperties(
                tempDir.toString(), "https://example.test", "/uploads", 1024 * 1024,
                java.util.List.of("image/jpeg", "image/png", "image/webp", "image/gif"),
                java.util.List.of("jpg", "jpeg", "png", "webp", "gif"));
        return new MediaStorageService(properties, mock(MediaFileRepository.class));
    }

    @Test
    void rejectsExecutableContentWithJpgExtension() {
        MockMultipartFile fakeJpeg = new MockMultipartFile(
                "file", "payload.jpg", "image/jpeg", "#!/bin/sh\nwhoami".getBytes());

        assertThatThrownBy(() -> service().uploadProductImage(fakeJpeg))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void rejectsSvgAndTraversalNames() {
        MockMultipartFile svg = new MockMultipartFile("file", "image.svg", "image/svg+xml", "<svg/>".getBytes());
        MockMultipartFile traversal = new MockMultipartFile("file", "../image.jpg", "image/jpeg", "x".getBytes());

        assertThatThrownBy(() -> service().uploadProductImage(svg)).isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> service().uploadProductImage(traversal)).isInstanceOf(BusinessException.class);
    }
}
