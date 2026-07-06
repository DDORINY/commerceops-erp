package com.commerceops.erp.domain.inventory.service;

import com.commerceops.erp.domain.audit.enums.AuditActionType;
import com.commerceops.erp.domain.audit.service.AuditLogService;
import com.commerceops.erp.domain.inventory.dto.InventoryAlertRuleActiveRequest;
import com.commerceops.erp.domain.inventory.dto.InventoryAlertRuleRequest;
import com.commerceops.erp.domain.inventory.dto.InventoryAlertRuleResponse;
import com.commerceops.erp.domain.inventory.dto.LowStockAlertResponse;
import com.commerceops.erp.domain.inventory.entity.InventoryAlertRule;
import com.commerceops.erp.domain.inventory.repository.InventoryAlertRuleRepository;
import com.commerceops.erp.domain.sku.entity.Sku;
import com.commerceops.erp.domain.sku.repository.SkuRepository;
import com.commerceops.erp.domain.user.entity.User;
import com.commerceops.erp.domain.warehouse.entity.Warehouse;
import com.commerceops.erp.domain.warehouse.repository.WarehouseLocationStockRepository;
import com.commerceops.erp.domain.warehouse.repository.WarehouseRepository;
import com.commerceops.erp.domain.warehouse.repository.WarehouseStockRepository;
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
public class InventoryAlertService {

    private final InventoryAlertRuleRepository ruleRepository;
    private final SkuRepository skuRepository;
    private final WarehouseRepository warehouseRepository;
    private final WarehouseLocationStockRepository locationStockRepository;
    private final WarehouseStockRepository warehouseStockRepository;
    private final AuditLogService auditLogService;

    public PageResponse<InventoryAlertRuleResponse> getRules(Long warehouseId, Boolean active, String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return PageResponse.from(ruleRepository.findAll(buildSpec(warehouseId, active, normalize(keyword)), pageable)
                .map(InventoryAlertRuleResponse::from));
    }

    @Transactional
    public InventoryAlertRuleResponse createRule(InventoryAlertRuleRequest request, User actor) {
        Sku sku = findSku(request.skuId());
        Warehouse warehouse = findWarehouseOrNull(request.warehouseId());
        validateUnique(sku.getId(), request.warehouseId(), null);

        InventoryAlertRule rule = ruleRepository.save(InventoryAlertRule.builder()
                .sku(sku)
                .warehouse(warehouse)
                .thresholdQuantity(request.thresholdQuantity())
                .memo(normalize(request.memo()))
                .active(true)
                .build());
        auditLogService.record(actor, AuditActionType.INVENTORY_ALERT_RULE_CREATED, "INVENTORY_ALERT_RULE", rule.getId(),
                null, String.valueOf(rule.getThresholdQuantity()), "안전재고 기준을 생성했습니다: " + sku.getSkuCode(),
                null, null, metadata(rule));
        return InventoryAlertRuleResponse.from(rule);
    }

    @Transactional
    public InventoryAlertRuleResponse updateRule(Long ruleId, InventoryAlertRuleRequest request, User actor) {
        InventoryAlertRule rule = findRule(ruleId);
        String before = summary(rule);
        Warehouse warehouse = findWarehouseOrNull(request.warehouseId());
        validateUnique(rule.getSku().getId(), request.warehouseId(), ruleId);
        rule.update(warehouse, request.thresholdQuantity(), normalize(request.memo()));
        auditLogService.record(actor, AuditActionType.INVENTORY_ALERT_RULE_UPDATED, "INVENTORY_ALERT_RULE", rule.getId(),
                before, summary(rule), "안전재고 기준을 수정했습니다: " + rule.getSku().getSkuCode(),
                null, null, metadata(rule));
        return InventoryAlertRuleResponse.from(rule);
    }

    @Transactional
    public InventoryAlertRuleResponse changeActive(Long ruleId, InventoryAlertRuleActiveRequest request, User actor) {
        InventoryAlertRule rule = findRule(ruleId);
        boolean before = rule.isActive();
        rule.changeActive(request.active());
        auditLogService.record(actor, AuditActionType.INVENTORY_ALERT_RULE_ACTIVE_CHANGED, "INVENTORY_ALERT_RULE", rule.getId(),
                String.valueOf(before), String.valueOf(rule.isActive()),
                "안전재고 기준 활성 상태를 변경했습니다: " + rule.getSku().getSkuCode(),
                null, null, metadata(rule));
        return InventoryAlertRuleResponse.from(rule);
    }

    public List<LowStockAlertResponse> getLowStockAlerts(Long warehouseId) {
        return ruleRepository.findAll(buildSpec(warehouseId, true, null), Sort.by("id").descending()).stream()
                .map(rule -> LowStockAlertResponse.from(rule, currentQuantity(rule)))
                .filter(alert -> alert.currentQuantity() <= alert.thresholdQuantity())
                .toList();
    }

    private int currentQuantity(InventoryAlertRule rule) {
        Long skuId = rule.getSku().getId();
        Long warehouseId = rule.getWarehouse() == null ? null : rule.getWarehouse().getId();
        long locationQuantity = locationStockRepository.sumQuantityBySkuAndWarehouse(skuId, warehouseId);
        if (locationQuantity > 0) {
            return Math.toIntExact(locationQuantity);
        }
        Long productId = rule.getSku().getProduct().getId();
        long fallbackQuantity = warehouseId == null
                ? warehouseStockRepository.sumQuantityByProductId(productId)
                : warehouseStockRepository.sumQuantityByWarehouseIdAndProductId(warehouseId, productId);
        return Math.toIntExact(fallbackQuantity);
    }

    private InventoryAlertRule findRule(Long ruleId) {
        return ruleRepository.findById(ruleId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "안전재고 기준을 찾을 수 없습니다."));
    }

    private Sku findSku(Long skuId) {
        return skuRepository.findById(skuId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "SKU를 찾을 수 없습니다."));
    }

    private Warehouse findWarehouseOrNull(Long warehouseId) {
        if (warehouseId == null) {
            return null;
        }
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WAREHOUSE_NOT_FOUND));
        if (!warehouse.isActive()) {
            throw new BusinessException(ErrorCode.WAREHOUSE_INACTIVE);
        }
        return warehouse;
    }

    private void validateUnique(Long skuId, Long warehouseId, Long ruleId) {
        boolean exists = warehouseId == null
                ? (ruleId == null ? ruleRepository.existsBySkuIdAndWarehouseIsNull(skuId) : ruleRepository.existsBySkuIdAndWarehouseIsNullAndIdNot(skuId, ruleId))
                : (ruleId == null ? ruleRepository.existsBySkuIdAndWarehouseId(skuId, warehouseId) : ruleRepository.existsBySkuIdAndWarehouseIdAndIdNot(skuId, warehouseId, ruleId));
        if (exists) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "이미 등록된 SKU/창고 안전재고 기준입니다.");
        }
    }

    private Specification<InventoryAlertRule> buildSpec(Long warehouseId, Boolean active, String keyword) {
        return (root, query, cb) -> {
            if (query != null && !Long.class.equals(query.getResultType())) {
                root.fetch("sku").fetch("product");
                root.fetch("warehouse", jakarta.persistence.criteria.JoinType.LEFT);
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
                        cb.like(cb.lower(root.get("sku").get("skuCode")), like),
                        cb.like(cb.lower(root.get("sku").get("barcode")), like),
                        cb.like(cb.lower(root.get("sku").get("product").get("name")), like)
                ));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String summary(InventoryAlertRule rule) {
        return rule.getSku().getSkuCode() + " / threshold=" + rule.getThresholdQuantity() + " / active=" + rule.isActive();
    }

    private String metadata(InventoryAlertRule rule) {
        return "{\"skuId\":" + rule.getSku().getId()
                + ",\"warehouseId\":" + (rule.getWarehouse() == null ? "null" : rule.getWarehouse().getId())
                + ",\"thresholdQuantity\":" + rule.getThresholdQuantity() + "}";
    }
}
