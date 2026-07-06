package com.commerceops.erp.domain.shipping.service;

import com.commerceops.erp.domain.audit.enums.AuditActionType;
import com.commerceops.erp.domain.audit.service.AuditLogService;
import com.commerceops.erp.domain.shipping.dto.CarrierRequest;
import com.commerceops.erp.domain.shipping.dto.CarrierResponse;
import com.commerceops.erp.domain.shipping.dto.ShippingMethodRequest;
import com.commerceops.erp.domain.shipping.dto.ShippingMethodResponse;
import com.commerceops.erp.domain.shipping.entity.Carrier;
import com.commerceops.erp.domain.shipping.entity.ShippingMethod;
import com.commerceops.erp.domain.shipping.repository.CarrierRepository;
import com.commerceops.erp.domain.shipping.repository.ShippingMethodRepository;
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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShippingSettingService {

    private final CarrierRepository carrierRepository;
    private final ShippingMethodRepository shippingMethodRepository;
    private final AuditLogService auditLogService;

    public PageResponse<CarrierResponse> getCarriers(String keyword, Boolean active, int page, int size) {
        var pageable = PageRequest.of(Math.max(page, 0), Math.max(1, Math.min(size, 100)),
                Sort.by("id").descending());
        return PageResponse.from(carrierRepository.findAll(buildCarrierSpec(keyword, active), pageable)
                .map(CarrierResponse::from));
    }

    @Transactional
    public CarrierResponse createCarrier(CarrierRequest request, User actor) {
        String code = normalizeCode(request.code());
        if (carrierRepository.existsByCodeIgnoreCase(code)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "이미 사용 중인 택배사 코드입니다.");
        }
        Carrier carrier = carrierRepository.save(Carrier.builder()
                .code(code)
                .name(normalizeRequired(request.name(), "택배사명을 입력해주세요."))
                .trackingUrlTemplate(normalize(request.trackingUrlTemplate()))
                .active(request.active() == null || request.active())
                .build());
        auditLogService.record(actor, AuditActionType.CARRIER_CREATED, "CARRIER", carrier.getId(),
                null, carrier.getCode(), "택배사를 생성했습니다: " + carrier.getName(), null, toCarrierJson(carrier), null);
        return CarrierResponse.from(carrier);
    }

    @Transactional
    public CarrierResponse updateCarrier(Long carrierId, CarrierRequest request, User actor) {
        Carrier carrier = findCarrier(carrierId);
        String beforeJson = toCarrierJson(carrier);
        String code = normalizeCode(request.code());
        if (carrierRepository.existsByCodeIgnoreCaseAndIdNot(code, carrierId)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "이미 사용 중인 택배사 코드입니다.");
        }
        carrier.update(code, normalizeRequired(request.name(), "택배사명을 입력해주세요."),
                normalize(request.trackingUrlTemplate()));
        auditLogService.record(actor, AuditActionType.CARRIER_UPDATED, "CARRIER", carrier.getId(),
                null, carrier.getCode(), "택배사를 수정했습니다: " + carrier.getName(), beforeJson, toCarrierJson(carrier), null);
        return CarrierResponse.from(carrier);
    }

    @Transactional
    public CarrierResponse updateCarrierActive(Long carrierId, boolean active, User actor) {
        Carrier carrier = findCarrier(carrierId);
        boolean before = Boolean.TRUE.equals(carrier.getActive());
        carrier.changeActive(active);
        auditLogService.record(actor, AuditActionType.CARRIER_ACTIVE_CHANGED, "CARRIER", carrier.getId(),
                String.valueOf(before), String.valueOf(active), "택배사 활성 상태를 변경했습니다: " + carrier.getName(),
                "{\"active\":" + before + "}", "{\"active\":" + active + "}", null);
        return CarrierResponse.from(carrier);
    }

    public PageResponse<ShippingMethodResponse> getShippingMethods(String keyword, Long carrierId, Boolean active,
                                                                   int page, int size) {
        var pageable = PageRequest.of(Math.max(page, 0), Math.max(1, Math.min(size, 100)),
                Sort.by("id").descending());
        return PageResponse.from(shippingMethodRepository.findAll(
                buildShippingMethodSpec(keyword, carrierId, active), pageable)
                .map(ShippingMethodResponse::from));
    }

    @Transactional
    public ShippingMethodResponse createShippingMethod(ShippingMethodRequest request, User actor) {
        String code = normalizeCode(request.code());
        if (shippingMethodRepository.existsByCodeIgnoreCase(code)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "이미 사용 중인 배송 방법 코드입니다.");
        }
        Carrier carrier = request.carrierId() != null ? findActiveCarrier(request.carrierId()) : null;
        ShippingMethod method = shippingMethodRepository.save(ShippingMethod.builder()
                .code(code)
                .name(normalizeRequired(request.name(), "배송 방법명을 입력해주세요."))
                .carrier(carrier)
                .defaultFee(resolveFee(request.defaultFee()))
                .description(normalize(request.description()))
                .active(request.active() == null || request.active())
                .build());
        auditLogService.record(actor, AuditActionType.SHIPPING_METHOD_CREATED, "SHIPPING_METHOD", method.getId(),
                null, method.getCode(), "배송 방법을 생성했습니다: " + method.getName(), null, toMethodJson(method), null);
        return ShippingMethodResponse.from(method);
    }

    @Transactional
    public ShippingMethodResponse updateShippingMethod(Long methodId, ShippingMethodRequest request, User actor) {
        ShippingMethod method = findShippingMethod(methodId);
        String beforeJson = toMethodJson(method);
        String code = normalizeCode(request.code());
        if (shippingMethodRepository.existsByCodeIgnoreCaseAndIdNot(code, methodId)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "이미 사용 중인 배송 방법 코드입니다.");
        }
        Carrier carrier = request.carrierId() != null ? findActiveCarrier(request.carrierId()) : null;
        method.update(code, normalizeRequired(request.name(), "배송 방법명을 입력해주세요."),
                carrier, resolveFee(request.defaultFee()), normalize(request.description()));
        auditLogService.record(actor, AuditActionType.SHIPPING_METHOD_UPDATED, "SHIPPING_METHOD", method.getId(),
                null, method.getCode(), "배송 방법을 수정했습니다: " + method.getName(), beforeJson, toMethodJson(method), null);
        return ShippingMethodResponse.from(method);
    }

    @Transactional
    public ShippingMethodResponse updateShippingMethodActive(Long methodId, boolean active, User actor) {
        ShippingMethod method = findShippingMethod(methodId);
        boolean before = Boolean.TRUE.equals(method.getActive());
        method.changeActive(active);
        auditLogService.record(actor, AuditActionType.SHIPPING_METHOD_ACTIVE_CHANGED, "SHIPPING_METHOD", method.getId(),
                String.valueOf(before), String.valueOf(active), "배송 방법 활성 상태를 변경했습니다: " + method.getName(),
                "{\"active\":" + before + "}", "{\"active\":" + active + "}", null);
        return ShippingMethodResponse.from(method);
    }

    private Carrier findCarrier(Long carrierId) {
        return carrierRepository.findById(carrierId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "택배사를 찾을 수 없습니다."));
    }

    private Carrier findActiveCarrier(Long carrierId) {
        Carrier carrier = findCarrier(carrierId);
        if (!Boolean.TRUE.equals(carrier.getActive())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "비활성 택배사는 배송 방법에 연결할 수 없습니다.");
        }
        return carrier;
    }

    private ShippingMethod findShippingMethod(Long methodId) {
        return shippingMethodRepository.findById(methodId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "배송 방법을 찾을 수 없습니다."));
    }

    private Specification<Carrier> buildCarrierSpec(String keyword, Boolean active) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (active != null) {
                predicates.add(cb.equal(root.get("active"), active));
            }
            String normalizedKeyword = normalize(keyword);
            if (normalizedKeyword != null) {
                String pattern = "%" + normalizedKeyword.toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("code")), pattern),
                        cb.like(cb.lower(root.get("name")), pattern)
                ));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Specification<ShippingMethod> buildShippingMethodSpec(String keyword, Long carrierId, Boolean active) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (carrierId != null) {
                predicates.add(cb.equal(root.get("carrier").get("id"), carrierId));
            }
            if (active != null) {
                predicates.add(cb.equal(root.get("active"), active));
            }
            String normalizedKeyword = normalize(keyword);
            if (normalizedKeyword != null) {
                String pattern = "%" + normalizedKeyword.toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("code")), pattern),
                        cb.like(cb.lower(root.get("name")), pattern),
                        cb.like(cb.lower(root.get("carrier").get("name")), pattern)
                ));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private String normalizeCode(String value) {
        return normalizeRequired(value, "코드를 입력해주세요.").toUpperCase(Locale.ROOT);
    }

    private String normalizeRequired(String value, String message) {
        String normalized = normalize(value);
        if (normalized == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, message);
        }
        return normalized;
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private Integer resolveFee(Integer value) {
        if (value == null || value < 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "기본 배송비는 0원 이상이어야 합니다.");
        }
        return value;
    }

    private String toCarrierJson(Carrier carrier) {
        return "{\"carrierId\":" + carrier.getId()
                + ",\"code\":\"" + escapeJson(carrier.getCode()) + "\""
                + ",\"active\":" + carrier.getActive()
                + "}";
    }

    private String toMethodJson(ShippingMethod method) {
        return "{\"shippingMethodId\":" + method.getId()
                + ",\"code\":\"" + escapeJson(method.getCode()) + "\""
                + ",\"carrierId\":" + (method.getCarrier() != null ? method.getCarrier().getId() : null)
                + ",\"defaultFee\":" + method.getDefaultFee()
                + ",\"active\":" + method.getActive()
                + "}";
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
