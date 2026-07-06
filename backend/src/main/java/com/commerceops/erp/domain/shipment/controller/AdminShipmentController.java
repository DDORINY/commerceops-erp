package com.commerceops.erp.domain.shipment.controller;

import com.commerceops.erp.domain.permission.PermissionCodes;
import com.commerceops.erp.domain.permission.service.PermissionChecker;
import com.commerceops.erp.domain.shipment.dto.ShipmentResponse;
import com.commerceops.erp.domain.shipment.dto.ShipmentLabelPreviewResponse;
import com.commerceops.erp.domain.shipment.dto.ShipmentLabelRequest;
import com.commerceops.erp.domain.shipment.dto.ShipmentLabelResponse;
import com.commerceops.erp.domain.shipment.dto.TrackingNumberGenerateRequest;
import com.commerceops.erp.domain.shipment.dto.TrackingUpdateRequest;
import com.commerceops.erp.domain.shipment.enums.ShipmentStatus;
import com.commerceops.erp.domain.shipment.service.ShipmentService;
import com.commerceops.erp.global.response.ApiResponse;
import com.commerceops.erp.global.response.PageResponse;
import com.commerceops.erp.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/shipments")
@RequiredArgsConstructor
public class AdminShipmentController {

    private final ShipmentService shipmentService;
    private final PermissionChecker permissionChecker;

    @GetMapping
    public ApiResponse<PageResponse<ShipmentResponse>> getShipments(
            @RequestParam(required = false) ShipmentStatus status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        permissionChecker.require(userDetails, PermissionCodes.SHIPMENT_READ);
        return ApiResponse.ok(shipmentService.getAdminShipments(status, keyword, page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<ShipmentResponse> getShipment(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.SHIPMENT_READ);
        return ApiResponse.ok(shipmentService.getAdminShipment(id));
    }

    @GetMapping("/{id}/labels")
    public ApiResponse<List<ShipmentLabelResponse>> getShipmentLabels(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        permissionChecker.require(userDetails, PermissionCodes.SHIPMENT_READ);
        return ApiResponse.ok(shipmentService.getShipmentLabels(id));
    }

    @PatchMapping("/{id}/tracking")
    public ApiResponse<ShipmentResponse> updateTracking(
            @PathVariable Long id,
            @Valid @RequestBody TrackingUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        permissionChecker.require(userDetails, PermissionCodes.SHIPMENT_MANAGE);
        return ApiResponse.ok(shipmentService.updateTracking(id, request, userDetails.getUser()));
    }

    @PostMapping("/{id}/tracking-number")
    public ApiResponse<ShipmentResponse> generateTrackingNumber(
            @PathVariable Long id,
            @Valid @RequestBody TrackingNumberGenerateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        permissionChecker.require(userDetails, PermissionCodes.SHIPMENT_MANAGE);
        return ApiResponse.ok(shipmentService.generateTrackingNumber(id, request.carrier(), userDetails.getUser()));
    }

    @PatchMapping("/{id}/tracking-number")
    public ApiResponse<ShipmentResponse> updateTrackingNumber(
            @PathVariable Long id,
            @Valid @RequestBody TrackingUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        permissionChecker.require(userDetails, PermissionCodes.SHIPMENT_MANAGE);
        return ApiResponse.ok(shipmentService.updateTracking(id, request, userDetails.getUser()));
    }

    @PostMapping("/{id}/labels")
    public ApiResponse<ShipmentLabelPreviewResponse> createShipmentLabel(
            @PathVariable Long id,
            @Valid @RequestBody(required = false) ShipmentLabelRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        permissionChecker.require(userDetails, PermissionCodes.SHIPPING_LABEL_PRINT);
        return ApiResponse.created("송장 라벨을 생성했습니다.",
                shipmentService.createShipmentLabel(id, request, userDetails.getUser()));
    }

    @PostMapping("/labels/{labelId}/print")
    public ApiResponse<ShipmentLabelPreviewResponse> markShipmentLabelPrinted(
            @PathVariable Long labelId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        permissionChecker.require(userDetails, PermissionCodes.SHIPPING_LABEL_PRINT);
        return ApiResponse.ok("송장 라벨 출력 이력을 기록했습니다.",
                shipmentService.markShipmentLabelPrinted(labelId, userDetails.getUser()));
    }

    @PatchMapping("/{id}/deliver")
    public ApiResponse<ShipmentResponse> markDelivered(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.SHIPMENT_MANAGE);
        return ApiResponse.ok(shipmentService.markDelivered(id));
    }
}
