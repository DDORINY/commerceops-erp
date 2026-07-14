package com.commerceops.erp.domain.order.dto;
import jakarta.validation.constraints.*;
public record ShippingAddressRequest(
 @NotBlank @Size(max=50) String addressName,
 @NotBlank @Size(max=50) String recipientName,
 @NotBlank @Pattern(regexp="^[0-9+() -]{8,20}$") String phone,
 @NotBlank @Pattern(regexp="^[0-9]{5,6}$") String postalCode,
 @NotBlank @Size(max=255) String roadAddress,
 @Size(max=255) String detailAddress, @Size(max=255) String extraAddress,
 @Size(max=255) String deliveryRequest, boolean saveAddress, boolean setAsDefault
) {}
