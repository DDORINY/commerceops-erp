package com.commerceops.erp.domain.order.service;

import com.commerceops.erp.domain.cart.entity.Cart;
import com.commerceops.erp.domain.cart.repository.CartRepository;
import com.commerceops.erp.domain.address.dto.AddressRequest;
import com.commerceops.erp.domain.address.entity.UserAddress;
import com.commerceops.erp.domain.address.repository.UserAddressRepository;
import com.commerceops.erp.domain.address.service.AddressService;
import com.commerceops.erp.domain.coupon.entity.Coupon;
import com.commerceops.erp.domain.coupon.repository.CouponRepository;
import com.commerceops.erp.domain.order.dto.*;
import com.commerceops.erp.domain.order.entity.Order;
import com.commerceops.erp.domain.order.entity.OrderItem;
import com.commerceops.erp.domain.order.entity.OrderAddress;
import com.commerceops.erp.domain.order.enums.OrderType;
import com.commerceops.erp.domain.order.enums.OrderStatus;
import com.commerceops.erp.domain.order.repository.OrderItemRepository;
import com.commerceops.erp.domain.order.repository.OrderRepository;
import com.commerceops.erp.domain.order.repository.OrderAddressRepository;
import com.commerceops.erp.domain.notification.enums.NotificationType;
import com.commerceops.erp.domain.notification.service.NotificationService;
import com.commerceops.erp.domain.payment.entity.Payment;
import com.commerceops.erp.domain.payment.enums.PaymentStatus;
import com.commerceops.erp.domain.payment.repository.PaymentRepository;
import com.commerceops.erp.domain.payment.dto.PaymentSummaryResponse;
import com.commerceops.erp.domain.product.entity.Product;
import com.commerceops.erp.domain.product.repository.ProductRepository;
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
import java.util.Map;
import java.util.UUID;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

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
    private final ProductRepository productRepository;
    private final UserAddressRepository userAddressRepository;
    private final AddressService addressService;
    private final OrderAddressRepository orderAddressRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public OrderCreateResponse createOrder(User user, OrderCreateRequest request) {
        validateOrderShape(request);
        List<Cart> cartItems = request.orderType() == OrderType.CART ? loadCartItems(user, request.cartItemIds()) : List.of();
        List<PurchaseLine> lines = request.orderType() == OrderType.CART
                ? cartItems.stream().map(c -> lockedLine(c.getProduct().getId(), c.getQuantity(), parseOptions(c.getSelectedOptions()))).toList()
                : List.of(lockedLine(request.productId(), request.quantity(), request.selectedOptions()));
        int totalPrice = lines.stream().mapToInt(l -> l.product().getPrice() * l.quantity()).sum();
        AddressData address = resolveAddress(user, request);

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

        int shippingFee = totalPrice >= 50_000 ? 0 : 3_000;
        int finalPrice = totalPrice - discountAmount + shippingFee;

        Order order = Order.builder()
                .user(user)
                .orderNumber("TEMP")
                .totalPrice(finalPrice)
                .discountAmount(discountAmount)
                .couponCode(appliedCouponCode)
                .status(OrderStatus.PENDING_PAYMENT)
                .paymentStatus(PaymentStatus.READY)
                .receiverName(address.recipientName()).receiverPhone(address.phone())
                .address(address.roadAddress()).detailAddress(address.detailAddress())
                .build();
        Order savedOrder = orderRepository.save(order);

        String orderNumber = String.format("ORD-%s-%06d",
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")),
                savedOrder.getId());
        savedOrder.updateOrderNumber(orderNumber);

        List<OrderItem> orderItems = lines.stream()
                .map(line -> OrderItem.builder()
                        .order(savedOrder)
                        .product(line.product()).productName(line.product().getName())
                        .price(line.product().getPrice()).quantity(line.quantity())
                        .selectedOptions(serializeOptions(line.options()))
                        .build())
                .toList();
        orderItemRepository.saveAll(orderItems);

        orderAddressRepository.save(OrderAddress.builder().order(savedOrder).recipientName(address.recipientName())
                .phone(address.phone()).postalCode(address.postalCode()).roadAddress(address.roadAddress())
                .detailAddress(address.detailAddress()).extraAddress(address.extraAddress())
                .deliveryRequest(address.deliveryRequest()).build());

        if (request.shippingAddress() != null && (request.shippingAddress().saveAddress() || request.shippingAddress().setAsDefault())) {
            ShippingAddressRequest a = request.shippingAddress();
            addressService.create(user, new AddressRequest(a.addressName(), a.recipientName(), a.phone(), a.postalCode(),
                    a.roadAddress(), a.detailAddress(), a.extraAddress(), a.deliveryRequest(), a.setAsDefault()));
        }

        lines.forEach(line -> line.product().decrementStock(line.quantity()));

        Payment payment = Payment.builder()
                .order(savedOrder)
                .paymentMethod(request.paymentMethod())
                .paymentStatus(PaymentStatus.READY)
                .paidAmount(0)
                .provider("TOSS")
                .providerOrderId(createProviderOrderId(savedOrder.getId()))
                .requestedAmount(savedOrder.getTotalPrice())
                .approvedAmount(0)
                .idempotencyKey(UUID.randomUUID().toString())
                .requestedAt(LocalDateTime.now())
                .build();
        paymentRepository.save(payment);

        if (request.orderType() == OrderType.CART) cartRepository.deleteAll(cartItems);

        return OrderCreateResponse.from(savedOrder);
    }

    private void validateOrderShape(OrderCreateRequest r) {
        boolean cart = r.orderType() == OrderType.CART;
        boolean cartValid = r.cartItemIds() != null && !r.cartItemIds().isEmpty() && r.productId() == null && r.quantity() == null;
        boolean buyNowValid = r.productId() != null && r.quantity() != null && r.quantity() > 0 && (r.cartItemIds() == null || r.cartItemIds().isEmpty());
        if ((cart && !cartValid) || (!cart && !buyNowValid)) throw new BusinessException(ErrorCode.INVALID_ORDER_TYPE);
        if ((r.savedAddressId() == null) == (r.shippingAddress() == null)) throw new BusinessException(ErrorCode.INVALID_SHIPPING_ADDRESS);
    }

    private List<Cart> loadCartItems(User user, List<Long> ids) {
        List<Long> unique = ids.stream().distinct().toList();
        List<Cart> items = cartRepository.findAllByIdInAndUserWithProduct(unique, user);
        if (items.size() != unique.size()) throw new BusinessException(ErrorCode.ORDER_ACCESS_DENIED);
        return items;
    }

    private PurchaseLine lockedLine(Long productId, int quantity, Map<String,String> options) {
        Product product = productRepository.findByIdForUpdate(productId).orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
        if (!product.isPurchasable(LocalDateTime.now())) throw new BusinessException(ErrorCode.PRODUCT_NOT_AVAILABLE);
        if (quantity < 1 || product.getStockQuantity() < quantity) throw new BusinessException(ErrorCode.OUT_OF_STOCK);
        validateOptions(product, options == null ? Map.of() : options);
        return new PurchaseLine(product, quantity, options == null ? Map.of() : options);
    }

    private void validateOptions(Product product, Map<String,String> selected) {
        List<com.commerceops.erp.domain.product.dto.ProductOptionGroup> groups = product.getOptions();
        if (groups == null || groups.isEmpty()) { if (!selected.isEmpty()) throw new BusinessException(ErrorCode.INVALID_ORDER_TYPE); return; }
        if (selected.size() != groups.size() || groups.stream().anyMatch(g -> !selected.containsKey(g.name()) || !g.values().contains(selected.get(g.name()))))
            throw new BusinessException(ErrorCode.INVALID_ORDER_TYPE);
    }

    private AddressData resolveAddress(User user, OrderCreateRequest r) {
        if (r.savedAddressId() != null) {
            UserAddress a = userAddressRepository.findByIdAndUser(r.savedAddressId(), user).orElseThrow(() -> new BusinessException(ErrorCode.ADDRESS_NOT_FOUND));
            return new AddressData(a.getRecipientName(),a.getPhone(),a.getPostalCode(),a.getRoadAddress(),a.getDetailAddress(),a.getExtraAddress(),a.getDeliveryRequest());
        }
        ShippingAddressRequest a=r.shippingAddress();
        return new AddressData(a.recipientName().trim(),a.phone().trim(),a.postalCode().trim(),a.roadAddress().trim(),clean(a.detailAddress()),clean(a.extraAddress()),clean(a.deliveryRequest()));
    }

    private Map<String,String> parseOptions(String json) { if (json == null || json.isBlank()) return Map.of(); try { return objectMapper.readValue(json,new TypeReference<>(){}); } catch(Exception e) { throw new BusinessException(ErrorCode.INVALID_ORDER_TYPE); } }
    private String serializeOptions(Map<String,String> options) { if(options==null||options.isEmpty()) return null; try{return objectMapper.writeValueAsString(new java.util.TreeMap<>(options));}catch(Exception e){throw new BusinessException(ErrorCode.INVALID_ORDER_TYPE);} }
    private String clean(String v){return v==null||v.isBlank()?null:v.trim();}
    private record PurchaseLine(Product product,int quantity,Map<String,String> options) {}
    private record AddressData(String recipientName,String phone,String postalCode,String roadAddress,String detailAddress,String extraAddress,String deliveryRequest) {}

    private String createProviderOrderId(Long orderId) {
        return "ORD-" + orderId + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
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
        return AdminOrderResponse.from(order, orderItemRepository.countByOrder(order),
                paymentRepository.findByOrder(order).orElse(null));
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
                    s.markInTransit();
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
        OrderAddress snapshot = orderAddressRepository.findByOrder(order).orElse(null);

        return new OrderDetailResponse(
                order.getId(),
                order.getOrderNumber(),
                snapshot == null ? order.getReceiverName() : snapshot.getRecipientName(),
                snapshot == null ? order.getReceiverPhone() : snapshot.getPhone(),
                snapshot == null ? null : snapshot.getPostalCode(),
                snapshot == null ? order.getAddress() : snapshot.getRoadAddress(),
                snapshot == null ? order.getDetailAddress() : snapshot.getDetailAddress(),
                snapshot == null ? null : snapshot.getExtraAddress(),
                snapshot == null ? null : snapshot.getDeliveryRequest(),
                order.getTotalPrice(),
                order.getStatus().name(),
                order.getPaymentStatus().name(),
                paymentRepository.findByOrder(order).map(PaymentSummaryResponse::from).orElse(null),
                items,
                order.getCreatedAt()
        );
    }

    private void validateStatusTransition(OrderStatus current, OrderStatus next) {
        boolean valid = switch (current) {
            case PENDING_PAYMENT, PAYMENT_FAILED, PENDING -> next == OrderStatus.CANCELLED;
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
