package com.commerceops.erp.domain.accounting.service;

import com.commerceops.erp.domain.accounting.dto.AccountingSummaryResponse;
import com.commerceops.erp.domain.accounting.entity.AccountingEntry;
import com.commerceops.erp.domain.accounting.enums.AccountingEntryType;
import com.commerceops.erp.domain.accounting.repository.AccountingEntryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountingServiceTest {

    @Mock
    private AccountingEntryRepository accountingEntryRepository;

    @InjectMocks
    private AccountingService accountingService;

    @Test
    void recordSalePersistsSaleEntryWithOrderReference() {
        accountingService.recordSale("ORD-20260704-000001", 45_000);

        ArgumentCaptor<AccountingEntry> captor = ArgumentCaptor.forClass(AccountingEntry.class);
        verify(accountingEntryRepository).save(captor.capture());

        AccountingEntry saved = captor.getValue();
        assertThat(saved.getType()).isEqualTo(AccountingEntryType.SALE);
        assertThat(saved.getAmount()).isEqualTo(45_000);
        assertThat(saved.getReferenceId()).isEqualTo("ORD-20260704-000001");
        assertThat(saved.getDescription()).contains("매출");
    }

    @Test
    void getSummaryCalculatesNetSalesFromSalesAndRefunds() {
        when(accountingEntryRepository.sumByType(AccountingEntryType.SALE)).thenReturn(100_000L);
        when(accountingEntryRepository.sumByType(AccountingEntryType.REFUND)).thenReturn(15_000L);
        when(accountingEntryRepository.sumByType(AccountingEntryType.INBOUND)).thenReturn(40_000L);

        AccountingSummaryResponse summary = accountingService.getSummary();

        assertThat(summary.totalSales()).isEqualTo(100_000L);
        assertThat(summary.totalRefunds()).isEqualTo(15_000L);
        assertThat(summary.totalInbound()).isEqualTo(40_000L);
        assertThat(summary.netSales()).isEqualTo(85_000L);
    }
}
