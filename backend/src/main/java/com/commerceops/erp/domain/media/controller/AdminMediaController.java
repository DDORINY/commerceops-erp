package com.commerceops.erp.domain.media.controller;

import com.commerceops.erp.domain.media.dto.MediaFileResponse;
import com.commerceops.erp.domain.media.service.MediaStorageService;
import com.commerceops.erp.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/media")
@RequiredArgsConstructor
public class AdminMediaController {

    private final MediaStorageService mediaStorageService;

    @PostMapping(value = "/product-images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<MediaFileResponse>> uploadProductImage(
            @RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(
                ApiResponse.ok("Product image uploaded.", mediaStorageService.uploadProductImage(file))
        );
    }
}
