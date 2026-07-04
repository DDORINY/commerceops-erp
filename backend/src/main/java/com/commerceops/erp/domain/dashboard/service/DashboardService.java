package com.commerceops.erp.domain.dashboard.service;

import com.commerceops.erp.domain.dashboard.dto.*;
import com.commerceops.erp.domain.order.enums.OrderStatus;
import com.commerceops.erp.domain.order.repository.OrderItemRepository;
import com.commerceops.erp.domain.order.repository.OrderRepository;
import com.commerceops.erp.domain.payment.enums.PaymentStatus;
import com.commerceops.erp.domain.payment.repository.PaymentRepository;
import com.commerceops.erp.domain.product.enums.ProductStatus;
import com.commerceops.erp.domain.product.repository.ProductRepository;
import com.commerceops.erp.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private static final int LOW_STOCK_THRESHOLD = 5;

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;

    public DashboardSummaryResponse getSummary() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        Long totalSales = paymentRepository.sumPaidAmountByStatus(PaymentStatus.PAID).orElse(0L);
        Long todaySales = paymentRepository.sumPaidAmountByStatusAndDate(PaymentStatus.PAID, startOfDay, endOfDay)
                .orElse(0L);
        Long totalOrders = orderRepository.count();
        Long todayOrders = orderRepository.countTodayOrders(startOfDay, endOfDay);
        Long totalCustomers = userRepository.count();
        Long totalProducts = productRepository.countByStatusNot(ProductStatus.DELETED);
        Long soldOutProductCount = productRepository.countByStatus(ProductStatus.SOLD_OUT);
        Long lowStockProductCount = productRepository.countByStockQuantityLessThanEqualAndStatusNot(
                LOW_STOCK_THRESHOLD, ProductStatus.DELETED);
        Long pendingOrderCount = orderRepository.countByStatus(OrderStatus.PENDING);
        Map<String, Long> orderStatusCounts = Arrays.stream(OrderStatus.values())
                .collect(Collectors.toMap(Enum::name, orderRepository::countByStatus));

        return new DashboardSummaryResponse(
                totalSales,
                todaySales,
                totalOrders,
                todayOrders,
                totalCustomers,
                totalProducts,
                soldOutProductCount,
                lowStockProductCount,
                pendingOrderCount,
                orderStatusCounts
        );
    }

    public List<SalesResponse> getSales(String period, LocalDate startDate, LocalDate endDate) {
        if (startDate == null) startDate = LocalDate.now().minusDays(6);
        if (endDate == null) endDate = LocalDate.now();

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();

        List<Object[]> rows = "MONTHLY".equalsIgnoreCase(period)
                ? paymentRepository.findMonthlySales(start, end)
                : paymentRepository.findDailySales(start, end);

        return rows.stream()
                .map(row -> new SalesResponse(
                        (String) row[0],
                        ((Number) row[1]).longValue(),
                        ((Number) row[2]).longValue()))
                .toList();
    }

    public List<LowStockProductResponse> getLowStockProducts(int limit) {
        return productRepository.findLowStockProducts(
                LOW_STOCK_THRESHOLD, ProductStatus.DELETED, PageRequest.of(0, limit))
                .stream()
                .map(LowStockProductResponse::from)
                .toList();
    }

    public List<TopProductResponse> getTopProducts(int limit) {
        return orderItemRepository.findTopProducts(PageRequest.of(0, limit))
                .stream()
                .map(row -> new TopProductResponse(
                        ((Number) row[0]).longValue(),
                        (String) row[1],
                        ((Number) row[2]).longValue(),
                        ((Number) row[3]).longValue()))
                .toList();
    }
}
