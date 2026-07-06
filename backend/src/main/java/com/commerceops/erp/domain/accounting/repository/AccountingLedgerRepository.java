package com.commerceops.erp.domain.accounting.repository;

import com.commerceops.erp.domain.accounting.entity.AccountingLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AccountingLedgerRepository extends JpaRepository<AccountingLedger, Long>, JpaSpecificationExecutor<AccountingLedger> {
}
