package com.commerceops.erp.domain.warehouse.service;

import com.commerceops.erp.domain.audit.enums.AuditActionType;
import com.commerceops.erp.domain.audit.service.AuditLogService;
import com.commerceops.erp.domain.user.entity.User;
import com.commerceops.erp.domain.warehouse.dto.WarehouseLocationActiveRequest;
import com.commerceops.erp.domain.warehouse.dto.WarehouseLocationCreateRequest;
import com.commerceops.erp.domain.warehouse.dto.WarehouseLocationResponse;
import com.commerceops.erp.domain.warehouse.dto.WarehouseLocationStockResponse;
import com.commerceops.erp.domain.warehouse.dto.WarehouseLocationUpdateRequest;
import com.commerceops.erp.domain.warehouse.entity.Warehouse;
import com.commerceops.erp.domain.warehouse.entity.WarehouseLocation;
import com.commerceops.erp.domain.warehouse.repository.WarehouseLocationRepository;
import com.commerceops.erp.domain.warehouse.repository.WarehouseLocationStockRepository;
import com.commerceops.erp.domain.warehouse.repository.WarehouseRepository;
import com.commerceops.erp.global.exception.BusinessException;
import com.commerceops.erp.global.exception.ErrorCode;
import com.commerceops.erp.global.response.PageResponse;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WarehouseLocationService {

    private final WarehouseRepository warehouseRepository;
    private final WarehouseLocationRepository warehouseLocationRepository;
    private final WarehouseLocationStockRepository warehouseLocationStockRepository;
    private final AuditLogService auditLogService;

    public PageResponse<WarehouseLocationResponse> getLocations(
            Long warehouseId, Boolean active, String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("code").ascending());
        return PageResponse.from(
                warehouseLocationRepository.findAll(buildSpec(warehouseId, active, normalize(keyword)), pageable)
                        .map(WarehouseLocationResponse::from)
        );
    }

    @Transactional
    public WarehouseLocationResponse createLocation(WarehouseLocationCreateRequest request, User actor) {
        Warehouse warehouse = findActiveWarehouse(request.warehouseId());
        String code = normalizeCode(request.code());
        if (warehouseLocationRepository.existsByWarehouseIdAndCodeIgnoreCase(warehouse.getId(), code)) {
            throw new BusinessException(ErrorCode.DUPLICATE_WAREHOUSE_LOCATION_CODE);
        }

        WarehouseLocation location = warehouseLocationRepository.save(WarehouseLocation.builder()
                .warehouse(warehouse)
                .code(code)
                .name(request.name().trim())
                .zone(normalize(request.zone()))
                .aisle(normalize(request.aisle()))
                .rack(normalize(request.rack()))
                .cell(normalize(request.cell()))
                .active(true)
                .build());

        auditLogService.record(actor, AuditActionType.WAREHOUSE_LOCATION_CREATED, "WAREHOUSE_LOCATION", location.getId(),
                null, location.getCode(), "창고 위치를 생성했습니다: " + location.getCode(),
                null, null, metadata(location));
        return WarehouseLocationResponse.from(location);
    }

    @Transactional
    public WarehouseLocationResponse updateLocation(Long locationId, WarehouseLocationUpdateRequest request, User actor) {
        WarehouseLocation location = findLocation(locationId);
        String before = summary(location);
        String code = normalizeCode(request.code());
        if (warehouseLocationRepository.existsByWarehouseIdAndCodeIgnoreCaseAndIdNot(location.getWarehouse().getId(), code, location.getId())) {
            throw new BusinessException(ErrorCode.DUPLICATE_WAREHOUSE_LOCATION_CODE);
        }

        location.update(
                code,
                request.name().trim(),
                normalize(request.zone()),
                normalize(request.aisle()),
                normalize(request.rack()),
                normalize(request.cell())
        );
        auditLogService.record(actor, AuditActionType.WAREHOUSE_LOCATION_UPDATED, "WAREHOUSE_LOCATION", location.getId(),
                before, summary(location), "창고 위치를 수정했습니다: " + location.getCode(),
                null, null, metadata(location));
        return WarehouseLocationResponse.from(location);
    }

    @Transactional
    public WarehouseLocationResponse changeActive(Long locationId, WarehouseLocationActiveRequest request, User actor) {
        WarehouseLocation location = findLocation(locationId);
        boolean before = location.isActive();
        location.changeActive(request.active());
        auditLogService.record(actor, AuditActionType.WAREHOUSE_LOCATION_ACTIVE_CHANGED, "WAREHOUSE_LOCATION", location.getId(),
                String.valueOf(before), String.valueOf(location.isActive()),
                "창고 위치 활성 상태를 변경했습니다: " + location.getCode(),
                null, null, metadata(location));
        return WarehouseLocationResponse.from(location);
    }

    public PageResponse<WarehouseLocationStockResponse> getLocationStocks(Long locationId, int page, int size) {
        if (!warehouseLocationRepository.existsById(locationId)) {
            throw new BusinessException(ErrorCode.WAREHOUSE_LOCATION_NOT_FOUND);
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return PageResponse.from(
                warehouseLocationStockRepository.findByLocationIdForAdmin(locationId, pageable)
                        .map(WarehouseLocationStockResponse::from)
        );
    }

    private WarehouseLocation findLocation(Long locationId) {
        return warehouseLocationRepository.findById(locationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WAREHOUSE_LOCATION_NOT_FOUND));
    }

    private Warehouse findActiveWarehouse(Long warehouseId) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WAREHOUSE_NOT_FOUND));
        if (!warehouse.isActive()) {
            throw new BusinessException(ErrorCode.WAREHOUSE_INACTIVE);
        }
        return warehouse;
    }

    private Specification<WarehouseLocation> buildSpec(Long warehouseId, Boolean active, String keyword) {
        return (root, query, cb) -> {
            if (query != null && !Long.class.equals(query.getResultType())) {
                root.fetch("warehouse");
                query.distinct(true);
            }
            List<Predicate> predicates = new ArrayList<>();
            if (warehouseId != null) {
                predicates.add(cb.equal(root.get("warehouse").get("id"), warehouseId));
            }
            if (active != null) {
                predicates.add(cb.equal(root.get("active"), active));
            }
            if (keyword != null) {
                String like = "%" + keyword.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("code")), like),
                        cb.like(cb.lower(root.get("name")), like),
                        cb.like(cb.lower(root.get("zone")), like),
                        cb.like(cb.lower(root.get("aisle")), like),
                        cb.like(cb.lower(root.get("rack")), like),
                        cb.like(cb.lower(root.get("cell")), like)
                ));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private String normalizeCode(String value) {
        return value.trim().toUpperCase();
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String summary(WarehouseLocation location) {
        return location.getCode() + " / " + location.getName() + " / active=" + location.isActive();
    }

    private String metadata(WarehouseLocation location) {
        return "{\"warehouseId\":" + location.getWarehouse().getId()
                + ",\"warehouseCode\":\"" + escapeJson(location.getWarehouse().getCode()) + "\""
                + ",\"locationCode\":\"" + escapeJson(location.getCode()) + "\"}";
    }

    private String escapeJson(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
