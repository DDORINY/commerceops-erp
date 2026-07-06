package com.commerceops.erp.domain.returns.service;

import com.commerceops.erp.domain.accounting.service.AccountingService;
import com.commerceops.erp.domain.audit.enums.AuditActionType;
import com.commerceops.erp.domain.audit.service.AuditLogService;
import com.commerceops.erp.domain.inventory.entity.InventoryLog;
import com.commerceops.erp.domain.inventory.enums.InventoryLogType;
import com.commerceops.erp.domain.inventory.repository.InventoryLogRepository;
import com.commerceops.erp.domain.notification.enums.NotificationType;
import com.commerceops.erp.domain.notification.service.NotificationService;
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
import com.commerceops.erp.domain.returns.dto.ReturnShipmentInfoRequest;
import com.commerceops.erp.domain.returns.dto.ReturnShipmentInfoResponse;
import com.commerceops.erp.domain.returns.entity.ReturnRequest;
import com.commerceops.erp.domain.returns.entity.ReturnShipmentInfo;
import com.commerceops.erp.domain.returns.enums.ReturnShipmentStatus;
import com.commerceops.erp.domain.returns.enums.ReturnShippingFeePayer;
import com.commerceops.erp.domain.returns.enums.ReturnStatus;
import com.commerceops.erp.domain.returns.repository.ReturnRequestRepository;
import com.commerceops.erp.domain.returns.repository.ReturnShipmentInfoRepository;
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
    private final ReturnShipmentInfoRepository returnShipmentInfoRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final InventoryLogRepository inventoryLogRepository;
    private final AccountingService accountingService;
    private final WarehouseFulfillmentService warehouseFulfillmentService;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

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
        notifyReturnProcessed(returnRequest);

        return ReturnResponse.from(returnRequest);
    }

    @Transactional
    public ReturnResponse rejectReturn(Long returnId, ReturnAdminActionRequest request) {
        ReturnRequest returnRequest = findReturn(returnId);
        if (returnRequest.getStatus() != ReturnStatus.REQUESTED) {
            throw new BusinessException(ErrorCode.RETURN_ALREADY_PROCESSED);
        }
        returnRequest.reject(request.adminNote());
        notifyReturnProcessed(returnRequest);
        return ReturnResponse.from(returnRequest);
    }

    public ReturnShipmentInfoResponse getReturnShipmentInfo(Long returnId) {
        ReturnRequest returnRequest = findReturn(returnId);
        return returnShipmentInfoRepository.findByReturnRequestId(returnRequest.getId())
                .map(ReturnShipmentInfoResponse::from)
                .orElseGet(() -> ReturnShipmentInfoResponse.from(createDefaultShipmentInfo(returnRequest)));
    }

    @Transactional
    public ReturnShipmentInfoResponse saveReturnShipmentInfo(Long returnId, ReturnShipmentInfoRequest request, User actor) {
        ReturnRequest returnRequest = findReturn(returnId);
        ReturnShipmentStatus nextStatus = request.status() != null
                ? request.status()
                : ReturnShipmentStatus.NOT_REQUESTED;
        ReturnShippingFeePayer nextFeePayer = request.feePayer() != null
                ? request.feePayer()
                : ReturnShippingFeePayer.UNDECIDED;

        ReturnShipmentInfo info = returnShipmentInfoRepository.findByReturnRequestId(returnRequest.getId())
                .orElse(null);
        boolean created = info == null;
        String beforeStatus = created ? null : info.getStatus().name();

        if (created) {
            info = ReturnShipmentInfo.builder()
                    .returnRequest(returnRequest)
                    .carrier(normalize(request.carrier()))
                    .trackingNumber(normalize(request.trackingNumber()))
                    .status(nextStatus)
                    .shippingFee(request.shippingFee())
                    .feePayer(nextFeePayer)
                    .memo(normalize(request.memo()))
                    .build();
        } else {
            info.update(
                    normalize(request.carrier()),
                    normalize(request.trackingNumber()),
                    nextStatus,
                    request.shippingFee(),
                    nextFeePayer,
                    normalize(request.memo())
            );
        }

        ReturnShipmentInfo saved = returnShipmentInfoRepository.save(info);
        AuditActionType action = resolveReturnShipmentAction(created, beforeStatus, saved.getStatus().name());
        auditLogService.record(actor, action, "RETURN_SHIPMENT", saved.getId(),
                beforeStatus, saved.getStatus().name(),
                "반품 배송 정보를 저장했습니다: 반품 ID " + returnRequest.getId());

        return ReturnShipmentInfoResponse.from(saved);
    }

    private void notifyReturnProcessed(ReturnRequest returnRequest) {
        notificationService.notifyUser(
                returnRequest.getUser(),
                NotificationType.RETURN_PROCESSED,
                "Return request processed",
                "Your return request for order " + returnRequest.getOrder().getOrderNumber()
                        + " is " + returnRequest.getStatus().name() + ".",
                "RETURN",
                returnRequest.getId()
        );
    }

    private ReturnRequest findReturn(Long returnId) {
        return returnRequestRepository.findById(returnId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RETURN_NOT_FOUND));
    }

    private ReturnShipmentInfo createDefaultShipmentInfo(ReturnRequest returnRequest) {
        return ReturnShipmentInfo.builder()
                .returnRequest(returnRequest)
                .status(ReturnShipmentStatus.NOT_REQUESTED)
                .feePayer(ReturnShippingFeePayer.UNDECIDED)
                .build();
    }

    private AuditActionType resolveReturnShipmentAction(boolean created, String beforeStatus, String afterStatus) {
        if (created) {
            return AuditActionType.RETURN_SHIPMENT_CREATED;
        }
        if (beforeStatus != null && !beforeStatus.equals(afterStatus)) {
            return AuditActionType.RETURN_SHIPMENT_STATUS_CHANGED;
        }
        return AuditActionType.RETURN_SHIPMENT_UPDATED;
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
