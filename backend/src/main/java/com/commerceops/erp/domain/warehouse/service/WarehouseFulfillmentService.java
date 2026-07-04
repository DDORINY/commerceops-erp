package com.commerceops.erp.domain.warehouse.service;

import com.commerceops.erp.domain.order.entity.Order;
import com.commerceops.erp.domain.order.entity.OrderItem;
import com.commerceops.erp.domain.warehouse.entity.StockReservation;
import com.commerceops.erp.domain.warehouse.entity.WarehouseStock;
import com.commerceops.erp.domain.warehouse.enums.StockReservationStatus;
import com.commerceops.erp.domain.warehouse.repository.StockReservationRepository;
import com.commerceops.erp.domain.warehouse.repository.WarehouseStockRepository;
import com.commerceops.erp.global.exception.BusinessException;
import com.commerceops.erp.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WarehouseFulfillmentService {

    private final WarehouseStockRepository warehouseStockRepository;
    private final StockReservationRepository stockReservationRepository;

    @Transactional
    public void reserveOrder(Order order, List<OrderItem> items) {
        if (stockReservationRepository.existsByOrder(order)) {
            return;
        }

        List<StockReservation> reservations = new ArrayList<>();
        for (OrderItem item : items) {
            int remaining = item.getQuantity();
            List<WarehouseStock> stocks = warehouseStockRepository.findAvailableForUpdate(item.getProduct().getId());
            for (WarehouseStock stock : stocks) {
                if (remaining == 0) break;
                int reserved = Math.min(stock.getAvailableQuantity(), remaining);
                if (reserved <= 0) continue;
                stock.reserve(reserved);
                reservations.add(StockReservation.builder()
                        .order(order)
                        .orderItem(item)
                        .warehouseStock(stock)
                        .quantity(reserved)
                        .status(StockReservationStatus.RESERVED)
                        .build());
                remaining -= reserved;
            }
            if (remaining > 0) {
                throw new BusinessException(ErrorCode.INSUFFICIENT_WAREHOUSE_STOCK);
            }
        }
        stockReservationRepository.saveAll(reservations);
    }

    @Transactional
    public void shipOrder(Order order) {
        List<StockReservation> reservations = stockReservationRepository.findAllByOrderAndStatus(
                order, StockReservationStatus.RESERVED);
        for (StockReservation reservation : reservations) {
            WarehouseStock stock = warehouseStockRepository.findForUpdate(
                            reservation.getWarehouseStock().getWarehouse().getId(),
                            reservation.getOrderItem().getProduct().getId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.WAREHOUSE_STOCK_NOT_FOUND));
            if (stock.getReservedQuantity() < reservation.getQuantity()
                    || stock.getQuantity() < reservation.getQuantity()) {
                throw new BusinessException(ErrorCode.INSUFFICIENT_WAREHOUSE_STOCK);
            }
            stock.shipReserved(reservation.getQuantity());
            reservation.markShipped();
        }
    }

    @Transactional
    public void releaseOrder(Order order) {
        List<StockReservation> reservations = stockReservationRepository.findAllByOrderAndStatus(
                order, StockReservationStatus.RESERVED);
        for (StockReservation reservation : reservations) {
            WarehouseStock stock = warehouseStockRepository.findForUpdate(
                            reservation.getWarehouseStock().getWarehouse().getId(),
                            reservation.getOrderItem().getProduct().getId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.WAREHOUSE_STOCK_NOT_FOUND));
            if (stock.getReservedQuantity() < reservation.getQuantity()) {
                throw new BusinessException(ErrorCode.INSUFFICIENT_WAREHOUSE_STOCK);
            }
            stock.releaseReserved(reservation.getQuantity());
            reservation.markReleased();
        }
    }

    @Transactional
    public void restoreReturnedOrder(Order order) {
        List<StockReservation> reservations = stockReservationRepository.findAllByOrderAndStatus(
                order, StockReservationStatus.SHIPPED);
        for (StockReservation reservation : reservations) {
            WarehouseStock stock = warehouseStockRepository.findForUpdate(
                            reservation.getWarehouseStock().getWarehouse().getId(),
                            reservation.getOrderItem().getProduct().getId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.WAREHOUSE_STOCK_NOT_FOUND));
            stock.increase(reservation.getQuantity());
            reservation.markReturned();
        }
    }
}
