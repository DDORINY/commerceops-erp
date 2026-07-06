package com.commerceops.erp.domain.sku.service;

import com.commerceops.erp.domain.audit.enums.AuditActionType;
import com.commerceops.erp.domain.audit.service.AuditLogService;
import com.commerceops.erp.domain.product.entity.Product;
import com.commerceops.erp.domain.product.repository.ProductRepository;
import com.commerceops.erp.domain.sku.dto.SkuCreateRequest;
import com.commerceops.erp.domain.sku.dto.SkuListResponse;
import com.commerceops.erp.domain.sku.dto.SkuResponse;
import com.commerceops.erp.domain.sku.dto.SkuUpdateRequest;
import com.commerceops.erp.domain.sku.entity.Sku;
import com.commerceops.erp.domain.sku.repository.SkuRepository;
import com.commerceops.erp.domain.user.entity.User;
import com.commerceops.erp.global.exception.BusinessException;
import com.commerceops.erp.global.exception.ErrorCode;
import com.commerceops.erp.global.response.PageResponse;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SkuService {

    private static final Pattern CODE_PATTERN = Pattern.compile("^[A-Za-z0-9._-]+$");

    private final SkuRepository skuRepository;
    private final ProductRepository productRepository;
    private final AuditLogService auditLogService;

    public PageResponse<SkuListResponse> getSkus(String keyword, Long productId, Boolean active,
                                                 Boolean hasBarcode, int page, int size) {
        var pageable = PageRequest.of(Math.max(page, 0), Math.max(1, Math.min(size, 100)),
                Sort.by("id").descending());
        return PageResponse.from(skuRepository.findAll(buildSpec(keyword, productId, active, hasBarcode), pageable)
                .map(SkuListResponse::from));
    }

    public SkuResponse getSku(Long skuId) {
        return SkuResponse.from(getSkuEntity(skuId));
    }

    public List<SkuResponse> getProductSkus(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        return skuRepository.findByProductIdOrderByIdAsc(productId).stream()
                .map(SkuResponse::from)
                .toList();
    }

    @Transactional
    public SkuResponse createSku(SkuCreateRequest request, User actor) {
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
        String skuCode = resolveCreateSkuCode(product, request.skuCode());
        String barcode = resolveCreateBarcode(product.getId(), request.barcode());
        String name = normalize(request.name());
        if (name == null) {
            name = defaultSkuName(product, request.optionSignature());
        }
        int safetyStockQuantity = request.safetyStockQuantity() != null
                ? request.safetyStockQuantity()
                : (product.getSafetyStockQuantity() != null ? product.getSafetyStockQuantity() : 0);

        Sku sku = skuRepository.save(Sku.builder()
                .product(product)
                .optionSignature(normalize(request.optionSignature()))
                .skuCode(skuCode)
                .barcode(barcode)
                .name(name)
                .safetyStockQuantity(safetyStockQuantity)
                .active(request.active() == null || request.active())
                .build());

        auditLogService.record(
                actor,
                AuditActionType.SKU_CREATED,
                "SKU",
                sku.getId(),
                null,
                sku.getSkuCode(),
                "SKU를 생성했습니다: " + sku.getSkuCode(),
                null,
                toJson(sku),
                "{\"productId\":" + product.getId() + "}"
        );
        return SkuResponse.from(sku);
    }

    @Transactional
    public SkuResponse updateSku(Long skuId, SkuUpdateRequest request, User actor) {
        Sku sku = getSkuEntity(skuId);
        String beforeJson = toJson(sku);
        String skuCode = normalize(request.skuCode());
        if (skuCode == null) {
            skuCode = sku.getSkuCode();
        } else {
            validateCode(skuCode, "SKU 코드는 영문, 숫자, 점, 하이픈, 밑줄만 사용할 수 있습니다.");
            if (skuRepository.existsBySkuCodeAndIdNot(skuCode, skuId)) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "이미 사용 중인 SKU 코드입니다.");
            }
        }

        String barcode = normalize(request.barcode());
        if (barcode == null) {
            barcode = sku.getBarcode();
        } else {
            validateCode(barcode, "바코드는 영문, 숫자, 점, 하이픈, 밑줄만 사용할 수 있습니다.");
            if (skuRepository.existsByBarcodeAndIdNot(barcode, skuId)) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "이미 사용 중인 바코드입니다.");
            }
        }

        String name = normalize(request.name());
        sku.update(
                normalize(request.optionSignature()),
                skuCode,
                barcode,
                name != null ? name : sku.getName(),
                request.safetyStockQuantity() != null ? request.safetyStockQuantity() : sku.getSafetyStockQuantity()
        );

        auditLogService.record(
                actor,
                AuditActionType.SKU_UPDATED,
                "SKU",
                sku.getId(),
                null,
                sku.getSkuCode(),
                "SKU 정보를 수정했습니다: " + sku.getSkuCode(),
                beforeJson,
                toJson(sku),
                "{\"productId\":" + sku.getProduct().getId() + "}"
        );
        return SkuResponse.from(sku);
    }

    @Transactional
    public SkuResponse updateActive(Long skuId, boolean active, User actor) {
        Sku sku = getSkuEntity(skuId);
        boolean before = Boolean.TRUE.equals(sku.getActive());
        sku.changeActive(active);
        auditLogService.record(
                actor,
                AuditActionType.SKU_ACTIVE_CHANGED,
                "SKU",
                sku.getId(),
                String.valueOf(before),
                String.valueOf(active),
                "SKU 활성 상태를 변경했습니다: " + sku.getSkuCode(),
                "{\"active\":" + before + "}",
                "{\"active\":" + active + "}",
                "{\"productId\":" + sku.getProduct().getId() + "}"
        );
        return SkuResponse.from(sku);
    }

    @Transactional
    public SkuResponse regenerateBarcode(Long skuId, User actor) {
        Sku sku = getSkuEntity(skuId);
        String before = sku.getBarcode();
        String barcode = generateUniqueBarcode(sku.getProduct().getId(), sku.getId());
        sku.changeBarcode(barcode);
        auditLogService.record(
                actor,
                AuditActionType.SKU_BARCODE_REGENERATED,
                "SKU",
                sku.getId(),
                before,
                barcode,
                "SKU 바코드를 재발급했습니다: " + sku.getSkuCode(),
                "{\"barcode\":\"" + escapeJson(before) + "\"}",
                "{\"barcode\":\"" + escapeJson(barcode) + "\"}",
                "{\"productId\":" + sku.getProduct().getId() + "}"
        );
        return SkuResponse.from(sku);
    }

    private Sku getSkuEntity(Long skuId) {
        return skuRepository.findById(skuId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "SKU를 찾을 수 없습니다."));
    }

    private Specification<Sku> buildSpec(String keyword, Long productId, Boolean active, Boolean hasBarcode) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (productId != null) {
                predicates.add(cb.equal(root.get("product").get("id"), productId));
            }
            if (active != null) {
                predicates.add(cb.equal(root.get("active"), active));
            }
            if (hasBarcode != null) {
                if (hasBarcode) {
                    predicates.add(cb.and(cb.isNotNull(root.get("barcode")), cb.notEqual(root.get("barcode"), "")));
                } else {
                    predicates.add(cb.or(cb.isNull(root.get("barcode")), cb.equal(root.get("barcode"), "")));
                }
            }
            String normalizedKeyword = normalize(keyword);
            if (normalizedKeyword != null) {
                String pattern = "%" + normalizedKeyword.toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("skuCode")), pattern),
                        cb.like(cb.lower(root.get("barcode")), pattern),
                        cb.like(cb.lower(root.get("name")), pattern),
                        cb.like(cb.lower(root.get("product").get("name")), pattern),
                        cb.like(cb.lower(root.get("product").get("productCode")), pattern)
                ));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private String resolveCreateSkuCode(Product product, String requestedCode) {
        String skuCode = normalize(requestedCode);
        if (skuCode != null) {
            validateCode(skuCode, "SKU 코드는 영문, 숫자, 점, 하이픈, 밑줄만 사용할 수 있습니다.");
            if (skuRepository.existsBySkuCode(skuCode)) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "이미 사용 중인 SKU 코드입니다.");
            }
            return skuCode;
        }
        return generateUniqueSkuCode(product);
    }

    private String resolveCreateBarcode(Long productId, String requestedBarcode) {
        String barcode = normalize(requestedBarcode);
        if (barcode != null) {
            validateCode(barcode, "바코드는 영문, 숫자, 점, 하이픈, 밑줄만 사용할 수 있습니다.");
            if (skuRepository.existsByBarcode(barcode)) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "이미 사용 중인 바코드입니다.");
            }
            return barcode;
        }
        return generateUniqueBarcode(productId, skuRepository.countByProductId(productId) + 1);
    }

    private String generateUniqueSkuCode(Product product) {
        String base = normalize(product.getProductCode());
        if (base == null) {
            base = "SKU-" + product.getId();
        }
        base = base.replaceAll("[^A-Za-z0-9._-]", "-");
        long sequence = skuRepository.countByProductId(product.getId()) + 1;
        String candidate;
        do {
            candidate = base + "-" + String.format("%03d", sequence++);
        } while (skuRepository.existsBySkuCode(candidate));
        return candidate;
    }

    private String generateUniqueBarcode(Long productId, long sequence) {
        String candidate;
        long next = Math.max(sequence, 1);
        do {
            candidate = "BC" + String.format("%06d", productId % 1_000_000) + String.format("%06d", next++);
        } while (skuRepository.existsByBarcode(candidate));
        return candidate;
    }

    private String defaultSkuName(Product product, String optionSignature) {
        String normalizedOption = normalize(optionSignature);
        return normalizedOption == null ? product.getName() : product.getName() + " / " + normalizedOption;
    }

    private void validateCode(String value, String message) {
        if (!CODE_PATTERN.matcher(value).matches()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, message);
        }
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String toJson(Sku sku) {
        return "{"
                + "\"skuId\":" + sku.getId()
                + ",\"productId\":" + sku.getProduct().getId()
                + ",\"skuCode\":\"" + escapeJson(sku.getSkuCode()) + "\""
                + ",\"barcode\":\"" + escapeJson(sku.getBarcode()) + "\""
                + ",\"active\":" + sku.getActive()
                + "}";
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
