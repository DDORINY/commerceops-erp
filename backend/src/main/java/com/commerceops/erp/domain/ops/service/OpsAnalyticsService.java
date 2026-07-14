package com.commerceops.erp.domain.ops.service;

import com.commerceops.erp.domain.accounting.enums.AccountingEntryType;
import com.commerceops.erp.domain.accounting.repository.AccountingEntryRepository;
import com.commerceops.erp.domain.ops.dto.OpsAnalyticsOverviewResponse;
import com.commerceops.erp.domain.order.enums.OrderStatus;
import com.commerceops.erp.domain.order.repository.OrderRepository;
import com.commerceops.erp.domain.payment.enums.PaymentStatus;
import com.commerceops.erp.domain.payment.repository.PaymentRepository;
import com.commerceops.erp.domain.warehouse.enums.StockReservationStatus;
import com.commerceops.erp.domain.warehouse.repository.StockReservationRepository;
import com.commerceops.erp.domain.warehouse.repository.WarehouseRepository;
import com.commerceops.erp.domain.warehouse.repository.WarehouseStockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OpsAnalyticsService {

    private final AccountingEntryRepository accountingEntryRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final WarehouseRepository warehouseRepository;
    private final WarehouseStockRepository warehouseStockRepository;
    private final StockReservationRepository stockReservationRepository;

    public OpsAnalyticsOverviewResponse getOverview() {
        long totalSales = accountingEntryRepository.sumByType(AccountingEntryType.SALE);
        long totalRefunds = accountingEntryRepository.sumByType(AccountingEntryType.REFUND);
        long totalInboundAmount = accountingEntryRepository.sumByType(AccountingEntryType.INBOUND);
        long netSales = totalSales - totalRefunds;
        long accountingEntryCount = accountingEntryRepository.count();

        long totalOrders = orderRepository.count();
        long paidOrders = orderRepository.countByStatus(OrderStatus.PAID);
        long cancelledOrders = orderRepository.countByStatus(OrderStatus.CANCELLED);
        long refundedOrders = orderRepository.countByStatus(OrderStatus.REFUNDED);
        long totalRevenue = paymentRepository.sumPaidAmountByStatus(PaymentStatus.PAID).orElse(0L)
                + paymentRepository.sumPaidAmountByStatus(PaymentStatus.DONE).orElse(0L);
        long averagePaidOrderAmount = paidOrders == 0 ? 0 : totalRevenue / paidOrders;
        Map<String, Long> orderStatusCounts = Arrays.stream(OrderStatus.values())
                .collect(Collectors.toMap(Enum::name, orderRepository::countByStatus));

        long totalWarehouses = warehouseRepository.count();
        long activeWarehouses = warehouseRepository.countByActiveTrue();
        long totalStockQuantity = warehouseStockRepository.sumQuantity();
        long totalReservedQuantity = warehouseStockRepository.sumReservedQuantity();
        long totalAvailableQuantity = warehouseStockRepository.sumAvailableQuantity();
        Map<String, Long> reservationStatusCounts = Arrays.stream(StockReservationStatus.values())
                .collect(Collectors.toMap(Enum::name, stockReservationRepository::countByStatus));

        return new OpsAnalyticsOverviewResponse(
                new OpsAnalyticsOverviewResponse.AccountingOverview(
                        totalSales,
                        totalRefunds,
                        totalInboundAmount,
                        netSales,
                        accountingEntryCount
                ),
                new OpsAnalyticsOverviewResponse.SalesOverview(
                        totalOrders,
                        paidOrders,
                        cancelledOrders,
                        refundedOrders,
                        totalRevenue,
                        averagePaidOrderAmount,
                        orderStatusCounts
                ),
                new OpsAnalyticsOverviewResponse.WarehouseOverview(
                        totalWarehouses,
                        activeWarehouses,
                        totalWarehouses - activeWarehouses,
                        totalStockQuantity,
                        totalReservedQuantity,
                        totalAvailableQuantity,
                        reservationStatusCounts
                ),
                "v0.2.8 foundation metrics for accounting, sales, and WMS planning."
        );
    }
}
