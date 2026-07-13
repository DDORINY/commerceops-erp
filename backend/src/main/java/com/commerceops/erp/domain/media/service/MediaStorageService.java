package com.commerceops.erp.domain.media.service;

import com.commerceops.erp.domain.media.config.MediaStorageProperties;
import com.commerceops.erp.domain.media.dto.MediaFileResponse;
import com.commerceops.erp.domain.media.entity.MediaFile;
import com.commerceops.erp.domain.media.repository.MediaFileRepository;
import com.commerceops.erp.global.exception.BusinessException;
import com.commerceops.erp.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MediaStorageService {

    private static final String PRODUCT_IMAGE_DIR = "product-images";

    private final MediaStorageProperties properties;
    private final MediaFileRepository mediaFileRepository;

    @Transactional
    public MediaFileResponse uploadProductImage(MultipartFile file) {
        validateImage(file);

        String extension = getExtension(file.getOriginalFilename());
        String storedFilename = UUID.randomUUID() + "." + extension;
        String datedDir = LocalDate.now().toString();
        Path root = Path.of(properties.uploadDir()).toAbsolutePath().normalize();
        Path targetDir = root.resolve(PRODUCT_IMAGE_DIR).resolve(datedDir).normalize();
        ensureInside(root, targetDir);

        try {
            Files.createDirectories(targetDir);
            Path target = targetDir.resolve(storedFilename).normalize();
            ensureInside(root, target);
            file.transferTo(target);

            String publicPath = normalizePublicPath(properties.publicPath());
            String relativeUrl = publicPath + "/" + PRODUCT_IMAGE_DIR + "/" + datedDir + "/" + storedFilename;
            String publicUrl = normalizeBaseUrl(properties.publicBaseUrl()) + relativeUrl;

            MediaFile mediaFile = MediaFile.builder()
                    .originalFilename(safeOriginalFilename(file.getOriginalFilename()))
                    .storedFilename(storedFilename)
                    .storagePath(root.relativize(target).toString().replace("\\", "/"))
                    .publicUrl(publicUrl)
                    .contentType(file.getContentType())
                    .size(file.getSize())
                    .mediaType("PRODUCT_IMAGE")
                    .build();

            return MediaFileResponse.from(mediaFileRepository.save(mediaFile));
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "File upload failed.");
        }
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "Image file is required.");
        }
        if (file.getSize() > properties.maxFileSize()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "Image file exceeds max size.");
        }

        String contentType = file.getContentType();
        if (contentType == null || properties.allowedContentTypes().stream().noneMatch(contentType::equalsIgnoreCase)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "Unsupported image content type.");
        }

        String extension = getExtension(file.getOriginalFilename());
        if (properties.allowedExtensions().stream().noneMatch(extension::equalsIgnoreCase)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "Unsupported image extension.");
        }
        validateImageSignature(file);
    }

    private void validateImageSignature(MultipartFile file) {
        try {
            BufferedImage decoded = ImageIO.read(file.getInputStream());
            if (decoded == null || decoded.getWidth() <= 0 || decoded.getHeight() <= 0) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "File is not a decodable image.");
            }
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "File could not be decoded as an image.");
        }
    }

    private String getExtension(String filename) {
        String cleaned = StringUtils.cleanPath(filename == null ? "" : filename);
        if (cleaned.contains("..") || cleaned.contains("/") || cleaned.contains("\\")) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "Invalid file name.");
        }
        int dotIndex = cleaned.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == cleaned.length() - 1) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "Image extension is required.");
        }
        return cleaned.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }

    private String safeOriginalFilename(String filename) {
        return StringUtils.cleanPath(filename == null ? "image" : filename);
    }

    private void ensureInside(Path root, Path target) {
        if (!target.normalize().startsWith(root.normalize())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "Invalid upload path.");
        }
    }

    private String normalizePublicPath(String publicPath) {
        String normalized = publicPath.startsWith("/") ? publicPath : "/" + publicPath;
        return normalized.endsWith("/") ? normalized.substring(0, normalized.length() - 1) : normalized;
    }

    private String normalizeBaseUrl(String baseUrl) {
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }
}
