package com.commerceops.erp.domain.barcode.service;

import com.commerceops.erp.domain.audit.enums.AuditActionType;
import com.commerceops.erp.domain.audit.service.AuditLogService;
import com.commerceops.erp.domain.barcode.dto.BarcodeLabelPreviewResponse;
import com.commerceops.erp.domain.barcode.dto.BarcodeLabelRequest;
import com.commerceops.erp.domain.barcode.dto.BarcodeLabelResponse;
import com.commerceops.erp.domain.barcode.dto.BarcodeSkuResponse;
import com.commerceops.erp.domain.barcode.entity.BarcodeLabel;
import com.commerceops.erp.domain.barcode.repository.BarcodeLabelRepository;
import com.commerceops.erp.domain.product.entity.Product;
import com.commerceops.erp.domain.sku.entity.Sku;
import com.commerceops.erp.domain.sku.repository.SkuRepository;
import com.commerceops.erp.domain.user.entity.User;
import com.commerceops.erp.global.exception.BusinessException;
import com.commerceops.erp.global.exception.ErrorCode;
import com.commerceops.erp.global.response.PageResponse;
import jakarta.persistence.criteria.Join;
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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BarcodeService {

    private static final String DEFAULT_FORMAT = "SKU_60X40";

    private final SkuRepository skuRepository;
    private final BarcodeLabelRepository barcodeLabelRepository;
    private final AuditLogService auditLogService;

    public PageResponse<BarcodeSkuResponse> searchSkus(String keyword, int page, int size) {
        var pageable = PageRequest.of(Math.max(page, 0), Math.max(1, Math.min(size, 100)), Sort.by("id").descending());
        return PageResponse.from(skuRepository.findAll(buildSkuSpec(keyword), pageable).map(BarcodeSkuResponse::from));
    }

    public BarcodeSkuResponse getByBarcode(String barcode) {
        String normalized = normalize(barcode);
        if (normalized == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "바코드를 입력해주세요.");
        }
        Sku sku = skuRepository.findAll((root, query, cb) -> cb.equal(root.get("barcode"), normalized)).stream()
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "바코드에 해당하는 SKU를 찾을 수 없습니다."));
        return BarcodeSkuResponse.from(sku);
    }

    public PageResponse<BarcodeLabelResponse> getLabels(String keyword, int page, int size) {
        var pageable = PageRequest.of(Math.max(page, 0), Math.max(1, Math.min(size, 100)), Sort.by("createdAt").descending());
        return PageResponse.from(barcodeLabelRepository.findAll(buildLabelSpec(keyword), pageable).map(BarcodeLabelResponse::from));
    }

    @Transactional
    public BarcodeLabelPreviewResponse createLabel(Long skuId, BarcodeLabelRequest request, User actor) {
        Sku sku = skuRepository.findById(skuId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "SKU를 찾을 수 없습니다."));
        if (!Boolean.TRUE.equals(sku.getActive())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "비활성 SKU는 라벨을 생성할 수 없습니다.");
        }
        if (normalize(sku.getBarcode()) == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "바코드가 없는 SKU는 라벨을 생성할 수 없습니다.");
        }

        String format = normalize(request != null ? request.labelFormat() : null);
        if (format == null) {
            format = DEFAULT_FORMAT;
        }

        BarcodeLabel label = BarcodeLabel.builder()
                .sku(sku)
                .barcode(sku.getBarcode())
                .labelFormat(format)
                .printCount(0)
                .createdBy(actor)
                .build();
        BarcodeLabel saved = barcodeLabelRepository.save(label);
        auditLogService.record(actor, AuditActionType.BARCODE_LABEL_CREATED, "BARCODE_LABEL", saved.getId(),
                null, saved.getBarcode(), "바코드 라벨을 생성했습니다: " + sku.getSkuCode(),
                null, null, metadata(saved));
        return toPreview(saved);
    }

    @Transactional
    public BarcodeLabelPreviewResponse markPrinted(Long labelId, User actor) {
        BarcodeLabel label = barcodeLabelRepository.findById(labelId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "바코드 라벨을 찾을 수 없습니다."));
        label.markPrinted();
        auditLogService.record(actor, AuditActionType.BARCODE_LABEL_PRINTED, "BARCODE_LABEL", label.getId(),
                null, String.valueOf(label.getPrintCount()), "바코드 라벨 출력 이력을 기록했습니다: " + label.getBarcode(),
                null, null, metadata(label));
        return toPreview(label);
    }

    private Specification<Sku> buildSkuSpec(String keyword) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            String normalized = normalize(keyword);
            if (normalized != null) {
                Join<Sku, Product> product = root.join("product");
                String pattern = "%" + normalized.toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("skuCode")), pattern),
                        cb.like(cb.lower(root.get("barcode")), pattern),
                        cb.like(cb.lower(root.get("name")), pattern),
                        cb.like(cb.lower(product.get("name")), pattern)
                ));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Specification<BarcodeLabel> buildLabelSpec(String keyword) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            String normalized = normalize(keyword);
            if (normalized != null) {
                Join<BarcodeLabel, Sku> sku = root.join("sku");
                Join<Sku, Product> product = sku.join("product");
                String pattern = "%" + normalized.toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("barcode")), pattern),
                        cb.like(cb.lower(sku.get("skuCode")), pattern),
                        cb.like(cb.lower(product.get("name")), pattern)
                ));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private BarcodeLabelPreviewResponse toPreview(BarcodeLabel label) {
        Sku sku = label.getSku();
        String html = "<div class=\"barcode-label\"><strong>" + escapeHtml(sku.getProduct().getName()) + "</strong>"
                + "<div>SKU: " + escapeHtml(sku.getSkuCode()) + "</div>"
                + "<div style=\"font-size:24px;letter-spacing:2px\">" + escapeHtml(label.getBarcode()) + "</div>"
                + "<div>" + escapeHtml(sku.getName()) + "</div></div>";
        return new BarcodeLabelPreviewResponse(label.getId(), label.getLabelFormat(), label.getBarcode(),
                sku.getSkuCode(), sku.getName(), sku.getProduct().getName(), html);
    }

    private String metadata(BarcodeLabel label) {
        return "{\"skuId\":" + label.getSku().getId()
                + ",\"productId\":" + label.getSku().getProduct().getId()
                + ",\"barcode\":\"" + escapeJson(label.getBarcode()) + "\""
                + ",\"labelFormat\":\"" + escapeJson(label.getLabelFormat()) + "\""
                + ",\"printCount\":" + label.getPrintCount() + "}";
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String escapeJson(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
