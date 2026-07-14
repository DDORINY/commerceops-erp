package com.commerceops.erp.domain.address.dto;

import com.commerceops.erp.domain.address.entity.UserAddress;
import java.time.LocalDateTime;

public record AddressResponse(Long addressId, String addressName, String recipientName, String phone,
 String postalCode, String roadAddress, String detailAddress, String extraAddress,
 String deliveryRequest, boolean isDefault, LocalDateTime createdAt, LocalDateTime updatedAt) {
 public static AddressResponse from(UserAddress a) { return new AddressResponse(a.getId(), a.getAddressName(),
  a.getRecipientName(), a.getPhone(), a.getPostalCode(), a.getRoadAddress(), a.getDetailAddress(),
  a.getExtraAddress(), a.getDeliveryRequest(), a.isDefault(), a.getCreatedAt(), a.getUpdatedAt()); }
}
