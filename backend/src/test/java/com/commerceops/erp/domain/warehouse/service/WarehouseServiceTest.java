package com.commerceops.erp.domain.warehouse.service;

import com.commerceops.erp.domain.product.entity.Product;
import com.commerceops.erp.domain.product.enums.ProductStatus;
import com.commerceops.erp.domain.product.repository.ProductRepository;
import com.commerceops.erp.domain.warehouse.dto.StockTransferResponse;
import com.commerceops.erp.domain.warehouse.dto.WarehouseStockAllocateRequest;
import com.commerceops.erp.domain.warehouse.entity.StockTransfer;
import com.commerceops.erp.domain.warehouse.entity.Warehouse;
import com.commerceops.erp.domain.warehouse.entity.WarehouseStock;
import com.commerceops.erp.domain.warehouse.enums.StockTransferStatus;
import com.commerceops.erp.domain.warehouse.repository.StockTransferRepository;
import com.commerceops.erp.domain.warehouse.repository.WarehouseRepository;
import com.commerceops.erp.domain.warehouse.repository.WarehouseStockRepository;
import com.commerceops.erp.global.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WarehouseServiceTest {

    @Mock private WarehouseRepository warehouseRepository;
    @Mock private WarehouseStockRepository warehouseStockRepository;
    @Mock private StockTransferRepository stockTransferRepository;
    @Mock private ProductRepository productRepository;

    @InjectMocks
    private WarehouseService warehouseService;

    @Test
    void allocationCannotExceedUnallocatedProductStock() {
        Warehouse warehouse = warehouse(1L, "SEOUL");
        Product product = product(10L, 20);
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(productRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(product));
        when(warehouseStockRepository.sumAvailableQuantityByProductId(10L)).thenReturn(15L);

        assertThatThrownBy(() -> warehouseService.allocateStock(
                new WarehouseStockAllocateRequest(1L, 10L, 6)
        )).isInstanceOf(BusinessException.class);
    }

    @Test
    void completingTransferMovesStockWithoutChangingTotal() {
        Warehouse from = warehouse(1L, "SEOUL");
        Warehouse to = warehouse(2L, "BUSAN");
        Product product = product(10L, 30);
        WarehouseStock source = WarehouseStock.builder()
                .id(100L).warehouse(from).product(product).quantity(20).build();
        WarehouseStock destination = WarehouseStock.builder()
                .id(101L).warehouse(to).product(product).quantity(10).build();
        StockTransfer transfer = StockTransfer.builder()
                .id(200L).transferNumber("TRF-20260704-TEST")
                .fromWarehouse(from).toWarehouse(to).product(product).quantity(7)
                .status(StockTransferStatus.PENDING).build();

        when(stockTransferRepository.findById(200L)).thenReturn(Optional.of(transfer));
        when(warehouseStockRepository.findForUpdate(1L, 10L)).thenReturn(Optional.of(source));
        when(warehouseStockRepository.findForUpdate(2L, 10L)).thenReturn(Optional.of(destination));
        when(warehouseStockRepository.save(any(WarehouseStock.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        StockTransferResponse response = warehouseService.completeStockTransfer(200L);

        assertThat(source.getQuantity()).isEqualTo(13);
        assertThat(destination.getQuantity()).isEqualTo(17);
        assertThat(source.getQuantity() + destination.getQuantity()).isEqualTo(30);
        assertThat(response.status()).isEqualTo("COMPLETED");
        assertThat(response.completedAt()).isNotNull();
    }

    @Test
    void completedTransferCannotBeCompletedAgain() {
        Warehouse from = warehouse(1L, "SEOUL");
        Warehouse to = warehouse(2L, "BUSAN");
        StockTransfer transfer = StockTransfer.builder()
                .id(201L).transferNumber("TRF-20260704-DONE")
                .fromWarehouse(from).toWarehouse(to).product(product(10L, 30)).quantity(3)
                .status(StockTransferStatus.COMPLETED).build();
        when(stockTransferRepository.findById(201L)).thenReturn(Optional.of(transfer));

        assertThatThrownBy(() -> warehouseService.completeStockTransfer(201L))
                .isInstanceOf(BusinessException.class);
    }

    private Warehouse warehouse(Long id, String code) {
        return Warehouse.builder()
                .id(id).code(code).name(code + " 창고").address("테스트 주소").active(true).build();
    }

    private Product product(Long id, int stock) {
        return Product.builder()
                .id(id).name("테스트 상품").price(10_000).stockQuantity(stock)
                .status(ProductStatus.ON_SALE).build();
    }
}
