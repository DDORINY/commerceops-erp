package com.commerceops.erp.domain.inventory.service;

import com.commerceops.erp.domain.accounting.service.AccountingService;
import com.commerceops.erp.domain.inventory.dto.InventoryInboundRequest;
import com.commerceops.erp.domain.inventory.repository.InventoryLogRepository;
import com.commerceops.erp.domain.product.entity.Product;
import com.commerceops.erp.domain.product.enums.ProductStatus;
import com.commerceops.erp.domain.product.repository.ProductRepository;
import com.commerceops.erp.domain.warehouse.entity.Warehouse;
import com.commerceops.erp.domain.warehouse.entity.WarehouseStock;
import com.commerceops.erp.domain.warehouse.repository.WarehouseRepository;
import com.commerceops.erp.domain.warehouse.repository.WarehouseStockRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private InventoryLogRepository inventoryLogRepository;
    @Mock private AccountingService accountingService;
    @Mock private WarehouseRepository warehouseRepository;
    @Mock private WarehouseStockRepository warehouseStockRepository;

    @InjectMocks
    private InventoryService inventoryService;

    @Test
    void inboundIncreasesProductAndTargetWarehouseStockTogether() {
        Warehouse warehouse = Warehouse.builder()
                .id(1L).code("SEOUL").name("서울 창고").address("서울").active(true).build();
        Product product = Product.builder()
                .id(2L).name("테스트 상품").price(10_000).stockQuantity(10)
                .status(ProductStatus.ON_SALE).build();
        WarehouseStock stock = WarehouseStock.builder()
                .id(3L).warehouse(warehouse).product(product).quantity(4).build();
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(productRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(product));
        when(warehouseStockRepository.findForUpdate(1L, 2L)).thenReturn(Optional.of(stock));
        when(warehouseStockRepository.save(any(WarehouseStock.class))).thenAnswer(i -> i.getArgument(0));

        inventoryService.inbound(new InventoryInboundRequest(1L, 2L, 3, "정기 입고"));

        assertThat(product.getStockQuantity()).isEqualTo(13);
        assertThat(stock.getQuantity()).isEqualTo(7);
        verify(accountingService).recordInbound(2L, "테스트 상품", 10_000, 3);
    }
}
