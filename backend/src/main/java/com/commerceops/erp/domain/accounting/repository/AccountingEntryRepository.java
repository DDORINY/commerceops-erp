package com.commerceops.erp.domain.accounting.repository;

import com.commerceops.erp.domain.accounting.entity.AccountingEntry;
import com.commerceops.erp.domain.accounting.enums.AccountingEntryType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AccountingEntryRepository extends JpaRepository<AccountingEntry, Long> {

    @Query("SELECT COALESCE(SUM(a.amount), 0) FROM AccountingEntry a WHERE a.type = :type")
    Long sumByType(@Param("type") AccountingEntryType type);

    @Query(
        value = "SELECT a FROM AccountingEntry a " +
                "WHERE (:type IS NULL OR a.type = :type)",
        countQuery = "SELECT COUNT(a) FROM AccountingEntry a " +
                     "WHERE (:type IS NULL OR a.type = :type)"
    )
    Page<AccountingEntry> findAllByType(
            @Param("type") AccountingEntryType type,
            Pageable pageable
    );
}
