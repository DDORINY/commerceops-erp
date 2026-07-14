package com.commerceops.erp.domain.address.service;

import com.commerceops.erp.domain.address.dto.*;
import com.commerceops.erp.domain.address.entity.UserAddress;
import com.commerceops.erp.domain.address.repository.UserAddressRepository;
import com.commerceops.erp.domain.user.entity.User;
import com.commerceops.erp.global.exception.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service @RequiredArgsConstructor @Transactional(readOnly = true)
public class AddressService {
 private final UserAddressRepository repository;
 public List<AddressResponse> getAddresses(User user) { return repository.findByUserOrderByIsDefaultDescUpdatedAtDesc(user).stream().map(AddressResponse::from).toList(); }
 @Transactional public AddressResponse create(User user, AddressRequest r) {
  boolean makeDefault = r.isDefault() || repository.countByUser(user) == 0;
  if (makeDefault) clearDefault(user);
  return AddressResponse.from(repository.save(UserAddress.builder().user(user).addressName(r.addressName().trim())
   .recipientName(r.recipientName().trim()).phone(r.phone().trim()).postalCode(r.postalCode().trim())
   .roadAddress(r.roadAddress().trim()).detailAddress(clean(r.detailAddress())).extraAddress(clean(r.extraAddress()))
   .deliveryRequest(clean(r.deliveryRequest())).isDefault(makeDefault).build()));
 }
 @Transactional public AddressResponse update(User user, Long id, AddressRequest r) {
  UserAddress a = owned(user, id); a.update(r.addressName().trim(), r.recipientName().trim(), r.phone().trim(),
   r.postalCode().trim(), r.roadAddress().trim(), clean(r.detailAddress()), clean(r.extraAddress()), clean(r.deliveryRequest()));
  if (r.isDefault()) { clearDefault(user); a.setDefault(true); }
  return AddressResponse.from(a);
 }
 @Transactional public void delete(User user, Long id) {
  UserAddress a = owned(user, id); boolean wasDefault = a.isDefault(); repository.delete(a); repository.flush();
  if (wasDefault) repository.findFirstByUserOrderByUpdatedAtDesc(user).ifPresent(next -> next.setDefault(true));
 }
 @Transactional public AddressResponse setDefault(User user, Long id) { UserAddress a=owned(user,id); clearDefault(user); a.setDefault(true); return AddressResponse.from(a); }
 public UserAddress owned(User user, Long id) { return repository.findByIdAndUser(id,user).orElseThrow(() -> new BusinessException(ErrorCode.ADDRESS_NOT_FOUND)); }
 private void clearDefault(User user) { repository.findFirstByUserAndIsDefaultTrue(user).ifPresent(a -> a.setDefault(false)); }
 private String clean(String value) { return value == null || value.isBlank() ? null : value.trim(); }
}
