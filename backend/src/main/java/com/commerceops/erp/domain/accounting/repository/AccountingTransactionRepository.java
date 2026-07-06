package com.commerceops.erp.domain.accounting.repository;

import com.commerceops.erp.domain.accounting.entity.AccountingTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AccountingTransactionRepository extends JpaRepository<AccountingTransaction, Long>, JpaSpecificationExecutor<AccountingTransaction> {
}
