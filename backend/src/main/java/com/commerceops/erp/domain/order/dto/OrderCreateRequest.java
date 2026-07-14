package com.commerceops.erp.domain.order.dto;

import com.commerceops.erp.domain.payment.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;
import com.commerceops.erp.domain.order.enums.OrderType;
import java.util.Map;

import java.util.List;

public record OrderCreateRequest(
        @NotNull OrderType orderType,
        @NotNull PaymentMethod paymentMethod,
        List<Long> cartItemIds,
        Long productId,
        Integer quantity,
        Map<String, String> selectedOptions,
        Long savedAddressId,
        @Valid ShippingAddressRequest shippingAddress,
        String couponCode
) {}
