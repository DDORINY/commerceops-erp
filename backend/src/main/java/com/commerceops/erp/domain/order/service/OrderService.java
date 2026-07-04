package com.commerceops.erp.domain.order.service;

import com.commerceops.erp.domain.cart.entity.Cart;
import com.commerceops.erp.domain.cart.repository.CartRepository;
import com.commerceops.erp.domain.coupon.entity.Coupon;
import com.commerceops.erp.domain.coupon.repository.CouponRepository;
import com.commerceops.erp.domain.order.dto.*;
import com.commerceops.erp.domain.order.entity.Order;
import com.commerceops.erp.domain.order.entity.OrderItem;
import com.commerceops.erp.domain.order.enums.OrderStatus;
import com.commerceops.erp.domain.order.repository.OrderItemRepository;
import com.commerceops.erp.domain.order.repository.OrderRepository;
import com.commerceops.erp.domain.notification.enums.NotificationType;
import com.commerceops.erp.domain.notification.service.NotificationService;
import com.commerceops.erp.domain.payment.entity.Payment;
import com.commerceops.erp.domain.payment.enums.PaymentStatus;
import com.commerceops.erp.domain.payment.repository.PaymentRepository;
import com.commerceops.erp.domain.product.entity.Product;
import com.commerceops.erp.domain.product.enums.ProductStatus;
import com.commerceops.erp.domain.shipment.entity.Shipment;
import com.commerceops.erp.domain.shipment.enums.ShipmentStatus;
import com.commerceops.erp.domain.shipment.repository.ShipmentRepository;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;
    private final CartRepository cartRepository;
    private final ShipmentRepository shipmentRepository;
    private final WarehouseFulfillmentService warehouseFulfillmentService;
    private final OrderCancellationService orderCancellationService;
    private final CouponRepository couponRepository;
    private final NotificationService notificationService;

    @Transactional
    public OrderCreateResponse createOrder(User user, OrderCreateRequest request) {
        List<Long> uniqueIds = request.cartItemIds().stream().distinct().toList();

        List<Cart> cartItems = cartRepository.findAllByIdInAndUserWithProduct(uniqueIds, user);
        if (cartItems.size() != uniqueIds.size()) {
            throw new BusinessException(ErrorCode.ORDER_ACCESS_DENIED);
        }

        int totalPrice = 0;
        for (Cart cart : cartItems) {
            Product product = cart.getProduct();
            if (product.getStatus() != ProductStatus.ON_SALE) {
                throw new BusinessException(ErrorCode.PRODUCT_NOT_AVAILABLE);
            }
            if (product.getStockQuantity() < cart.getQuantity()) {
                throw new BusinessException(ErrorCode.OUT_OF_STOCK);
            }
            totalPrice += product.getPrice() * cart.getQuantity();
        }

        int discountAmount = 0;
        String appliedCouponCode = null;
        if (request.couponCode() != null && !request.couponCode().isBlank()) {
            Coupon coupon = couponRepository.findByCode(request.couponCode().toUpperCase())
                    .orElseThrow(() -> new BusinessException(ErrorCode.COUPON_NOT_FOUND));
            if (!coupon.isValid(totalPrice)) {
                if (!coupon.isActive()) throw new BusinessException(ErrorCode.COUPON_INACTIVE);
                if (java.time.LocalDateTime.now().isAfter(coupon.getExpiresAt())) throw new BusinessException(ErrorCode.COUPON_EXPIRED);
                if (coupon.getUsedCount() >= coupon.getMaxUsage()) throw new BusinessException(ErrorCode.COUPON_EXHAUSTED);
                throw new BusinessException(ErrorCode.COUPON_MIN_ORDER_NOT_MET);
            }
            discountAmount = coupon.calculateDiscount(totalPrice);
            appliedCouponCode = coupon.getCode();
            coupon.use();
        }

        int finalPrice = totalPrice - discountAmount;

        Order order = Order.builder()
                .user(user)
                .orderNumber("TEMP")
                .totalPrice(finalPrice)
                .discountAmount(discountAmount)
                .couponCode(appliedCouponCode)
                .status(OrderStatus.PENDING)
                .paymentStatus(PaymentStatus.READY)
                .receiverName(request.receiverName())
                .receiverPhone(request.receiverPhone())
                .address(request.address())
                .detailAddress(request.detailAddress())
                .build();
        Order savedOrder = orderRepository.save(order);

        String orderNumber = String.format("ORD-%s-%06d",
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")),
                savedOrder.getId());
        savedOrder.updateOrderNumber(orderNumber);

        List<OrderItem> orderItems = cartItems.stream()
                .map(cart -> OrderItem.builder()
                        .order(savedOrder)
                        .product(cart.getProduct())
                        .productName(cart.getProduct().getName())
                        .price(cart.getProduct().getPrice())
                        .quantity(cart.getQuantity())
                        .selectedOptions(cart.getSelectedOptions())
                        .build())
                .toList();
        orderItemRepository.saveAll(orderItems);

        Payment payment = Payment.builder()
                .order(savedOrder)
                .paymentMethod(request.paymentMethod())
                .paymentStatus(PaymentStatus.READY)
                .paidAmount(0)
                .build();
        paymentRepository.save(payment);

        cartRepository.deleteAll(cartItems);

        return OrderCreateResponse.from(savedOrder);
    }

    public List<OrderResponse> getOrders(User user) {
        return orderRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(OrderResponse::from)
                .toList();
    }

    public PageResponse<AdminOrderResponse> getAdminOrders(OrderStatus status, String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return PageResponse.from(
                orderRepository.findAllForAdmin(status, keyword, pageable).map(this::toAdminOrderResponse));
    }

    private AdminOrderResponse toAdminOrderResponse(Order order) {
        return AdminOrderResponse.from(order, orderItemRepository.countByOrder(order));
    }

    @Transactional
    public OrderStatusUpdateResponse updateOrderStatus(Long orderId, OrderStatusUpdateRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        validateStatusTransition(order.getStatus(), request.status());
        if (request.status() == OrderStatus.CANCELLED) {
            orderCancellationService.cancel(order);
            notifyOrderStatus(order);
            return new OrderStatusUpdateResponse(order.getId(), order.getStatus().name());
        }
        order.updateStatus(request.status());

        syncShipmentStatus(order, request.status());
        notifyOrderStatus(order);

        return new OrderStatusUpdateResponse(order.getId(), order.getStatus().name());
    }

    @Transactional
    public OrderStatusUpdateResponse cancelOrder(User user, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        if (!order.getUser().getId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.ORDER_ACCESS_DENIED);
        }
        validateStatusTransition(order.getStatus(), OrderStatus.CANCELLED);
        orderCancellationService.cancel(order);
        notifyOrderStatus(order);
        return new OrderStatusUpdateResponse(order.getId(), order.getStatus().name());
    }

    private void syncShipmentStatus(Order order, OrderStatus newStatus) {
        if (newStatus == OrderStatus.PREPARING) {
            boolean exists = shipmentRepository.findByOrderId(order.getId()).isPresent();
            if (!exists) {
                shipmentRepository.save(Shipment.builder()
                        .order(order)
                        .status(ShipmentStatus.READY)
                        .build());
            }
        } else if (newStatus == OrderStatus.SHIPPING) {
            warehouseFulfillmentService.shipOrder(order);
            shipmentRepository.findByOrderId(order.getId()).ifPresent(s -> {
                if (s.getStatus() != ShipmentStatus.IN_TRANSIT && s.getStatus() != ShipmentStatus.DELIVERED) {
                    s.updateTracking(s.getTrackingNumber(), s.getCarrier());
                }
            });
        } else if (newStatus == OrderStatus.COMPLETED) {
            shipmentRepository.findByOrderId(order.getId()).ifPresent(s -> {
                if (s.getStatus() != ShipmentStatus.DELIVERED) {
                    s.markDelivered();
                }
            });
        }
    }

    public OrderDetailResponse getOrderDetail(User user, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.ORDER_ACCESS_DENIED);
        }

        List<OrderItemResponse> items = orderItemRepository.findAllByOrderWithProduct(order)
                .stream()
                .map(OrderItemResponse::from)
                .toList();

        return new OrderDetailResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getReceiverName(),
                order.getReceiverPhone(),
                order.getAddress(),
                order.getDetailAddress(),
                order.getTotalPrice(),
                order.getStatus().name(),
                order.getPaymentStatus().name(),
                items,
                order.getCreatedAt()
        );
    }

    private void validateStatusTransition(OrderStatus current, OrderStatus next) {
        boolean valid = switch (current) {
            case PENDING -> next == OrderStatus.CANCELLED;
            case PAID -> next == OrderStatus.PREPARING || next == OrderStatus.CANCELLED;
            case PREPARING -> next == OrderStatus.SHIPPING || next == OrderStatus.CANCELLED;
            case SHIPPING -> next == OrderStatus.COMPLETED;
            default -> false;
        };
        if (!valid) {
            throw new BusinessException(ErrorCode.INVALID_ORDER_STATUS);
        }
    }

    private void notifyOrderStatus(Order order) {
        notificationService.notifyUser(
                order.getUser(),
                NotificationType.ORDER_STATUS,
                "Order status updated",
                "Order " + order.getOrderNumber() + " status is now " + order.getStatus().name() + ".",
                "ORDER",
                order.getId()
        );
    }
}
