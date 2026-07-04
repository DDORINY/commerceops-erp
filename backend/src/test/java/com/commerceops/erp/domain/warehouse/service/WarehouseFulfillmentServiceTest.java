package com.commerceops.erp.domain.warehouse.service;

import com.commerceops.erp.domain.order.entity.Order;
import com.commerceops.erp.domain.order.entity.OrderItem;
import com.commerceops.erp.domain.product.entity.Product;
import com.commerceops.erp.domain.product.enums.ProductStatus;
import com.commerceops.erp.domain.warehouse.entity.StockReservation;
import com.commerceops.erp.domain.warehouse.entity.Warehouse;
import com.commerceops.erp.domain.warehouse.entity.WarehouseStock;
import com.commerceops.erp.domain.warehouse.enums.StockReservationStatus;
import com.commerceops.erp.domain.warehouse.repository.StockReservationRepository;
import com.commerceops.erp.domain.warehouse.repository.WarehouseStockRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WarehouseFulfillmentServiceTest {

    @Mock private WarehouseStockRepository warehouseStockRepository;
    @Mock private StockReservationRepository stockReservationRepository;

    @InjectMocks
    private WarehouseFulfillmentService fulfillmentService;

    @Test
    @SuppressWarnings("unchecked")
    void reservationShipmentAndReturnPreserveWarehouseLifecycle() {
        Order order = Order.builder().id(1L).orderNumber("ORD-1").build();
        Product product = Product.builder()
                .id(10L).name("테스트 상품").price(10_000).stockQuantity(13)
                .status(ProductStatus.ON_SALE).build();
        OrderItem item = OrderItem.builder()
                .id(20L).order(order).product(product).productName(product.getName())
                .price(product.getPrice()).quantity(5).build();
        Warehouse firstWarehouse = warehouse(100L, "A");
        Warehouse secondWarehouse = warehouse(101L, "B");
        WarehouseStock firstStock = WarehouseStock.builder()
                .id(200L).warehouse(firstWarehouse).product(product).quantity(3).build();
        WarehouseStock secondStock = WarehouseStock.builder()
                .id(201L).warehouse(secondWarehouse).product(product).quantity(10).build();

        when(stockReservationRepository.existsByOrder(order)).thenReturn(false);
        when(warehouseStockRepository.findAvailableForUpdate(product.getId()))
                .thenReturn(List.of(firstStock, secondStock));

        fulfillmentService.reserveOrder(order, List.of(item));

        ArgumentCaptor<List<StockReservation>> captor = ArgumentCaptor.forClass(List.class);
        verify(stockReservationRepository).saveAll(captor.capture());
        List<StockReservation> reservations = captor.getValue();
        assertThat(reservations).extracting(StockReservation::getQuantity).containsExactly(3, 2);
        assertThat(firstStock.getReservedQuantity()).isEqualTo(3);
        assertThat(secondStock.getReservedQuantity()).isEqualTo(2);

        when(stockReservationRepository.findAllByOrderAndStatus(order, StockReservationStatus.RESERVED))
                .thenReturn(reservations);
        when(warehouseStockRepository.findForUpdate(firstWarehouse.getId(), product.getId()))
                .thenReturn(Optional.of(firstStock));
        when(warehouseStockRepository.findForUpdate(secondWarehouse.getId(), product.getId()))
                .thenReturn(Optional.of(secondStock));

        fulfillmentService.shipOrder(order);

        assertThat(firstStock.getQuantity()).isZero();
        assertThat(secondStock.getQuantity()).isEqualTo(8);
        assertThat(reservations).allMatch(r -> r.getStatus() == StockReservationStatus.SHIPPED);

        when(stockReservationRepository.findAllByOrderAndStatus(order, StockReservationStatus.SHIPPED))
                .thenReturn(reservations);
        fulfillmentService.restoreReturnedOrder(order);

        assertThat(firstStock.getQuantity()).isEqualTo(3);
        assertThat(secondStock.getQuantity()).isEqualTo(10);
        assertThat(reservations).allMatch(r -> r.getStatus() == StockReservationStatus.RETURNED);
    }

    @Test
    void existingOrderReservationMakesReserveIdempotent() {
        Order order = Order.builder().id(1L).orderNumber("ORD-1").build();
        when(stockReservationRepository.existsByOrder(order)).thenReturn(true);

        fulfillmentService.reserveOrder(order, List.of());

        verify(warehouseStockRepository, never()).findAvailableForUpdate(1L);
        verify(stockReservationRepository, never()).saveAll(anyList());
    }

    @Test
    void releaseOrderRestoresAvailableQuantityOnlyOnce() {
        Order order = Order.builder().id(1L).orderNumber("ORD-1").build();
        Product product = Product.builder().id(10L).name("상품").stockQuantity(5).build();
        Warehouse warehouse = warehouse(100L, "A");
        WarehouseStock stock = WarehouseStock.builder()
                .id(200L).warehouse(warehouse).product(product).quantity(5).reservedQuantity(2).build();
        OrderItem item = OrderItem.builder().id(20L).order(order).product(product).quantity(2).build();
        StockReservation reservation = StockReservation.builder()
                .id(300L).order(order).orderItem(item).warehouseStock(stock).quantity(2)
                .status(StockReservationStatus.RESERVED).build();
        when(stockReservationRepository.findAllByOrderAndStatus(order, StockReservationStatus.RESERVED))
                .thenReturn(List.of(reservation), List.of());
        when(warehouseStockRepository.findForUpdate(warehouse.getId(), product.getId()))
                .thenReturn(Optional.of(stock));

        fulfillmentService.releaseOrder(order);
        fulfillmentService.releaseOrder(order);

        assertThat(stock.getQuantity()).isEqualTo(5);
        assertThat(stock.getReservedQuantity()).isZero();
        assertThat(reservation.getStatus()).isEqualTo(StockReservationStatus.RELEASED);
    }

    private Warehouse warehouse(Long id, String code) {
        return Warehouse.builder().id(id).code(code).name(code).address("테스트").active(true).build();
    }
}
