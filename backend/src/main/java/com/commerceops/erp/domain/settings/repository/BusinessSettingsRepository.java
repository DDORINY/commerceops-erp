package com.commerceops.erp.domain.settings.repository;

import com.commerceops.erp.domain.settings.entity.BusinessSettings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BusinessSettingsRepository extends JpaRepository<BusinessSettings, Long> {

    Optional<BusinessSettings> findFirstByOrderByIdAsc();
}
