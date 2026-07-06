package com.commerceops.erp.domain.settings.service;

import com.commerceops.erp.domain.audit.enums.AuditActionType;
import com.commerceops.erp.domain.audit.service.AuditLogService;
import com.commerceops.erp.domain.settings.dto.BusinessSettingsResponse;
import com.commerceops.erp.domain.settings.dto.BusinessSettingsUpdateRequest;
import com.commerceops.erp.domain.settings.dto.PublicBusinessSettingsResponse;
import com.commerceops.erp.domain.settings.dto.PublicTermsVersionResponse;
import com.commerceops.erp.domain.settings.dto.TermsVersionCreateRequest;
import com.commerceops.erp.domain.settings.dto.TermsVersionResponse;
import com.commerceops.erp.domain.settings.entity.BusinessSettings;
import com.commerceops.erp.domain.settings.entity.TermsVersion;
import com.commerceops.erp.domain.settings.enums.TermsType;
import com.commerceops.erp.domain.settings.repository.BusinessSettingsRepository;
import com.commerceops.erp.domain.settings.repository.TermsVersionRepository;
import com.commerceops.erp.domain.user.entity.User;
import com.commerceops.erp.global.exception.BusinessException;
import com.commerceops.erp.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SettingsService {

    private final BusinessSettingsRepository businessSettingsRepository;
    private final TermsVersionRepository termsVersionRepository;
    private final AuditLogService auditLogService;

    public BusinessSettingsResponse getCompanySettings() {
        return businessSettingsRepository.findFirstByOrderByIdAsc()
                .map(BusinessSettingsResponse::from)
                .orElseGet(BusinessSettingsResponse::empty);
    }

    public PublicBusinessSettingsResponse getPublicCompanySettings() {
        return businessSettingsRepository.findFirstByOrderByIdAsc()
                .map(PublicBusinessSettingsResponse::from)
                .orElseGet(PublicBusinessSettingsResponse::empty);
    }

    @Transactional
    public BusinessSettingsResponse updateCompanySettings(BusinessSettingsUpdateRequest request, User actor) {
        BusinessSettings settings = businessSettingsRepository.findFirstByOrderByIdAsc()
                .orElseGet(() -> businessSettingsRepository.save(BusinessSettings.builder().updatedBy(actor).build()));
        BusinessSettingsResponse before = BusinessSettingsResponse.from(settings);
        settings.update(
                clean(request.companyName()),
                clean(request.representativeName()),
                clean(request.businessRegistrationNumber()),
                clean(request.mailOrderBusinessNumber()),
                clean(request.address()),
                clean(request.customerServicePhone()),
                clean(request.customerServiceEmail()),
                clean(request.brandName()),
                actor
        );
        BusinessSettingsResponse response = BusinessSettingsResponse.from(settings);
        auditLogService.record(
                actor,
                AuditActionType.BUSINESS_SETTINGS_UPDATED,
                "BUSINESS_SETTINGS",
                settings.getId(),
                null,
                "updated",
                "사업자 설정을 수정했습니다.",
                toCompanyAuditJson(before),
                toCompanyAuditJson(response),
                "{\"changedFields\":\"business_settings\"}"
        );
        return response;
    }

    public List<TermsVersionResponse> getTermsVersions() {
        return termsVersionRepository.findAllByOrderByTypeAscCreatedAtDesc().stream()
                .map(TermsVersionResponse::from)
                .toList();
    }

    public List<TermsVersionResponse> getTermsVersions(TermsType type) {
        return termsVersionRepository.findByTypeOrderByCreatedAtDesc(type).stream()
                .map(TermsVersionResponse::from)
                .toList();
    }

    public TermsVersionResponse getTermsVersion(TermsType type, Long versionId) {
        return termsVersionRepository.findByIdAndType(versionId, type)
                .map(TermsVersionResponse::from)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
    }

    public TermsVersionResponse getLatestTerms(TermsType type) {
        return TermsVersionResponse.from(findLatestTerms(type));
    }

    public PublicTermsVersionResponse getPublicLatestTerms(TermsType type) {
        return PublicTermsVersionResponse.from(findLatestTerms(type));
    }

    private TermsVersion findLatestTerms(TermsType type) {
        return termsVersionRepository
                .findFirstByTypeAndActiveTrueAndEffectiveFromLessThanEqualOrderByEffectiveFromDescCreatedAtDesc(type, LocalDateTime.now())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
    }

    @Transactional
    public TermsVersionResponse createTermsVersion(TermsVersionCreateRequest request, User actor) {
        TermsType type = request.type();
        termsVersionRepository.findByTypeAndActiveTrue(type).forEach(TermsVersion::deactivate);
        String version = clean(request.version());
        if (version == null) {
            version = "v" + (termsVersionRepository.countByType(type) + 1);
        }
        LocalDateTime effectiveFrom = request.effectiveFrom() != null ? request.effectiveFrom() : LocalDateTime.now();
        TermsVersion saved = termsVersionRepository.save(TermsVersion.builder()
                .type(type)
                .title(clean(request.title()))
                .content(request.content().trim())
                .version(version)
                .effectiveFrom(effectiveFrom)
                .active(true)
                .createdBy(actor)
                .build());
        auditLogService.record(
                actor,
                type == TermsType.TERMS_OF_SERVICE ? AuditActionType.TERMS_VERSION_CREATED : AuditActionType.POLICY_VERSION_CREATED,
                "TERMS_VERSION",
                saved.getId(),
                null,
                saved.getVersion(),
                type + " 새 버전을 생성했습니다.",
                null,
                null,
                "{\"type\":\"" + type + "\",\"version\":\"" + escapeJson(saved.getVersion()) + "\",\"title\":\"" + escapeJson(saved.getTitle()) + "\",\"contentLength\":" + saved.getContent().length() + "}"
        );
        return TermsVersionResponse.from(saved);
    }

    private String clean(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String toCompanyAuditJson(BusinessSettingsResponse response) {
        if (response == null || response.id() == null) {
            return "{}";
        }
        return "{"
                + "\"companyName\":\"" + escapeJson(response.companyName()) + "\","
                + "\"representativeName\":\"" + escapeJson(response.representativeName()) + "\","
                + "\"businessRegistrationNumber\":\"" + escapeJson(response.businessRegistrationNumber()) + "\","
                + "\"mailOrderBusinessNumber\":\"" + escapeJson(response.mailOrderBusinessNumber()) + "\","
                + "\"phone\":\"" + escapeJson(response.customerServicePhone()) + "\","
                + "\"email\":\"" + escapeJson(response.customerServiceEmail()) + "\""
                + "}";
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
