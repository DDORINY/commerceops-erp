package com.commerceops.erp.domain.payment.dto;

import jakarta.validation.constraints.NotNull;

public record TossPaymentPrepareRequest(@NotNull Long orderId) {}
