package com.commerceops.erp.domain.settings.repository;

import com.commerceops.erp.domain.settings.entity.TermsVersion;
import com.commerceops.erp.domain.settings.enums.TermsType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TermsVersionRepository extends JpaRepository<TermsVersion, Long> {

    List<TermsVersion> findAllByOrderByTypeAscCreatedAtDesc();

    List<TermsVersion> findByTypeOrderByCreatedAtDesc(TermsType type);

    List<TermsVersion> findByTypeAndActiveTrue(TermsType type);

    Optional<TermsVersion> findFirstByTypeAndActiveTrueAndEffectiveFromLessThanEqualOrderByEffectiveFromDescCreatedAtDesc(
            TermsType type,
            LocalDateTime now
    );

    Optional<TermsVersion> findByIdAndType(Long id, TermsType type);

    long countByType(TermsType type);
}
