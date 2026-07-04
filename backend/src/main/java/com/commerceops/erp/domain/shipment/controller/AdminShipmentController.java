package com.commerceops.erp.domain.shipment.controller;

import com.commerceops.erp.domain.shipment.dto.ShipmentResponse;
import com.commerceops.erp.domain.shipment.dto.TrackingUpdateRequest;
import com.commerceops.erp.domain.shipment.enums.ShipmentStatus;
import com.commerceops.erp.domain.shipment.service.ShipmentService;
import com.commerceops.erp.global.response.ApiResponse;
import com.commerceops.erp.global.response.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/shipments")
@RequiredArgsConstructor
public class AdminShipmentController {

    private final ShipmentService shipmentService;

    @GetMapping
    public ApiResponse<PageResponse<ShipmentResponse>> getShipments(
            @RequestParam(required = false) ShipmentStatus status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size
    ) {
        return ApiResponse.ok(shipmentService.getAdminShipments(status, keyword, page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<ShipmentResponse> getShipment(@PathVariable Long id) {
        return ApiResponse.ok(shipmentService.getAdminShipment(id));
    }

    @PatchMapping("/{id}/tracking")
    public ApiResponse<ShipmentResponse> updateTracking(
            @PathVariable Long id,
            @Valid @RequestBody TrackingUpdateRequest request
    ) {
        return ApiResponse.ok(shipmentService.updateTracking(id, request));
    }

    @PatchMapping("/{id}/deliver")
    public ApiResponse<ShipmentResponse> markDelivered(@PathVariable Long id) {
        return ApiResponse.ok(shipmentService.markDelivered(id));
    }
}
