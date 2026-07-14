package com.commerceops.erp.domain.address.controller;

import com.commerceops.erp.domain.address.dto.*;
import com.commerceops.erp.domain.address.service.AddressService;
import com.commerceops.erp.global.response.ApiResponse;
import com.commerceops.erp.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/addresses") @RequiredArgsConstructor
public class AddressController {
 private final AddressService service;
 @GetMapping public ResponseEntity<ApiResponse<List<AddressResponse>>> list(@AuthenticationPrincipal CustomUserDetails u) { return ResponseEntity.ok(ApiResponse.ok("배송지 목록 조회가 완료되었습니다.", service.getAddresses(u.getUser()))); }
 @PostMapping public ResponseEntity<ApiResponse<AddressResponse>> create(@AuthenticationPrincipal CustomUserDetails u, @Valid @RequestBody AddressRequest r) { return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created("배송지가 등록되었습니다.", service.create(u.getUser(),r))); }
 @PutMapping("/{id}") public ResponseEntity<ApiResponse<AddressResponse>> update(@AuthenticationPrincipal CustomUserDetails u,@PathVariable Long id,@Valid @RequestBody AddressRequest r) { return ResponseEntity.ok(ApiResponse.ok("배송지가 수정되었습니다.",service.update(u.getUser(),id,r))); }
 @DeleteMapping("/{id}") public ResponseEntity<ApiResponse<Void>> delete(@AuthenticationPrincipal CustomUserDetails u,@PathVariable Long id) { service.delete(u.getUser(),id); return ResponseEntity.ok(ApiResponse.noContent("배송지가 삭제되었습니다.")); }
 @PatchMapping("/{id}/default") public ResponseEntity<ApiResponse<AddressResponse>> setDefault(@AuthenticationPrincipal CustomUserDetails u,@PathVariable Long id) { return ResponseEntity.ok(ApiResponse.ok("기본 배송지가 변경되었습니다.",service.setDefault(u.getUser(),id))); }
}
