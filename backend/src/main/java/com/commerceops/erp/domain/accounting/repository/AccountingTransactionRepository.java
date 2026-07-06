package com.commerceops.erp.domain.accounting.repository;

import com.commerceops.erp.domain.accounting.entity.AccountingTransaction;
import com.commerceops.erp.domain.accounting.enums.AccountingReferenceType;
import com.commerceops.erp.domain.accounting.enums.AccountingTransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface AccountingTransactionRepository extends JpaRepository<AccountingTransaction, Long>, JpaSpecificationExecutor<AccountingTransaction> {

    boolean existsByReferenceTypeAndReferenceIdAndType(
            AccountingReferenceType referenceType,
            Long referenceId,
            AccountingTransactionType type
    );

    Optional<AccountingTransaction> findFirstByReferenceTypeAndReferenceIdAndTypeOrderByOccurredAtDesc(
            AccountingReferenceType referenceType,
            Long referenceId,
            AccountingTransactionType type
    );
}
