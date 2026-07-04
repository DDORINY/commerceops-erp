package com.commerceops.erp.domain.returns.service;

import com.commerceops.erp.domain.accounting.service.AccountingService;
import com.commerceops.erp.domain.inventory.entity.InventoryLog;
import com.commerceops.erp.domain.inventory.enums.InventoryLogType;
import com.commerceops.erp.domain.inventory.repository.InventoryLogRepository;
import com.commerceops.erp.domain.order.entity.Order;
import com.commerceops.erp.domain.order.entity.OrderItem;
import com.commerceops.erp.domain.order.enums.OrderStatus;
import com.commerceops.erp.domain.order.repository.OrderItemRepository;
import com.commerceops.erp.domain.order.repository.OrderRepository;
import com.commerceops.erp.domain.product.entity.Product;
import com.commerceops.erp.domain.product.repository.ProductRepository;
import com.commerceops.erp.domain.returns.dto.ReturnAdminActionRequest;
import com.commerceops.erp.domain.returns.dto.ReturnCreateRequest;
import com.commerceops.erp.domain.returns.dto.ReturnResponse;
import com.commerceops.erp.domain.returns.entity.ReturnRequest;
import com.commerceops.erp.domain.returns.enums.ReturnStatus;
import com.commerceops.erp.domain.returns.repository.ReturnRequestRepository;
import com.commerceops.erp.domain.user.entity.User;
import com.commerceops.erp.domain.warehouse.service.WarehouseFulfillmentService;
import com.commerceops.erp.global.exception.BusinessException;
import com.commerceops.erp.global.exception.ErrorCode;
import com.commerceops.erp.global.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReturnService {

    private final ReturnRequestRepository returnRequestRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final InventoryLogRepository inventoryLogRepository;
    private final AccountingService accountingService;
    private final WarehouseFulfillmentService warehouseFulfillmentService;

    @Transactional
    public ReturnResponse createReturn(Long orderId, User user, ReturnCreateRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.ORDER_ACCESS_DENIED);
        }

        if (order.getStatus() != OrderStatus.SHIPPING && order.getStatus() != OrderStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.ORDER_NOT_RETURNABLE);
        }

        boolean alreadyRequested = returnRequestRepository.existsByOrderIdAndStatusNot(
                orderId, ReturnStatus.REJECTED);
        if (alreadyRequested) {
            throw new BusinessException(ErrorCode.RETURN_ALREADY_REQUESTED);
        }

        ReturnRequest returnRequest = ReturnRequest.builder()
                .order(order)
                .user(user)
                .reason(request.reason())
                .reasonDetail(request.reasonDetail())
                .status(ReturnStatus.REQUESTED)
                .build();

        return ReturnResponse.from(returnRequestRepository.save(returnRequest));
    }

    public List<ReturnResponse> getMyReturns(User user) {
        return returnRequestRepository.findByUserOrderByCreatedAtDesc(user)
                .stream().map(ReturnResponse::from).toList();
    }

    public PageResponse<ReturnResponse> getAdminReturns(ReturnStatus status, String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return PageResponse.from(
                returnRequestRepository.findAllForAdmin(status, keyword, pageable).map(ReturnResponse::from));
    }

    @Transactional
    public ReturnResponse approveReturn(Long returnId, ReturnAdminActionRequest request) {
        ReturnRequest returnRequest = findReturn(returnId);
        if (returnRequest.getStatus() != ReturnStatus.REQUESTED) {
            throw new BusinessException(ErrorCode.RETURN_ALREADY_PROCESSED);
        }

        returnRequest.approve(request.adminNote());

        // 재고 복구
        Order order = returnRequest.getOrder();
        warehouseFulfillmentService.restoreReturnedOrder(order);
        List<OrderItem> items = orderItemRepository.findAllByOrderWithProduct(order);
        for (OrderItem item : items) {
            Product product = item.getProduct();
            int before = product.getStockQuantity();
            product.incrementStock(item.getQuantity());
            inventoryLogRepository.save(InventoryLog.builder()
                    .product(product)
                    .type(InventoryLogType.RETURN_RESTOCK)
                    .quantity(item.getQuantity())
                    .beforeStock(before)
                    .afterStock(product.getStockQuantity())
                    .memo("반품 승인 - 주문번호: " + order.getOrderNumber())
                    .build());
        }

        order.updateStatus(OrderStatus.REFUNDED);

        accountingService.recordRefund(order.getOrderNumber(), order.getTotalPrice());

        return ReturnResponse.from(returnRequest);
    }

    @Transactional
    public ReturnResponse rejectReturn(Long returnId, ReturnAdminActionRequest request) {
        ReturnRequest returnRequest = findReturn(returnId);
        if (returnRequest.getStatus() != ReturnStatus.REQUESTED) {
            throw new BusinessException(ErrorCode.RETURN_ALREADY_PROCESSED);
        }
        returnRequest.reject(request.adminNote());
        return ReturnResponse.from(returnRequest);
    }

    private ReturnRequest findReturn(Long returnId) {
        return returnRequestRepository.findById(returnId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RETURN_NOT_FOUND));
    }
}
