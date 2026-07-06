package com.commerceops.erp.domain.returns.dto;

import com.commerceops.erp.domain.returns.enums.ReturnShipmentStatus;
import com.commerceops.erp.domain.returns.enums.ReturnShippingFeePayer;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ReturnShipmentInfoRequest(
        @Size(max = 100) String carrier,
        @Size(max = 100) String trackingNumber,
        ReturnShipmentStatus status,
        @DecimalMin(value = "0.00") BigDecimal shippingFee,
        ReturnShippingFeePayer feePayer,
        @Size(max = 500) String memo
) {
}
