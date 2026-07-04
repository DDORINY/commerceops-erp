package com.commerceops.erp.domain.coupon.service;

import com.commerceops.erp.domain.coupon.dto.CouponCreateRequest;
import com.commerceops.erp.domain.coupon.dto.CouponResponse;
import com.commerceops.erp.domain.coupon.dto.CouponValidateResponse;
import com.commerceops.erp.domain.coupon.entity.Coupon;
import com.commerceops.erp.domain.coupon.repository.CouponRepository;
import com.commerceops.erp.global.exception.BusinessException;
import com.commerceops.erp.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CouponService {

    private final CouponRepository couponRepository;

    @Transactional
    public CouponResponse createCoupon(CouponCreateRequest request) {
        if (couponRepository.existsByCode(request.code())) {
            throw new BusinessException(ErrorCode.DUPLICATE_COUPON_CODE);
        }
        Coupon coupon = Coupon.builder()
                .code(request.code().toUpperCase())
                .discountType(request.discountType())
                .discountValue(request.discountValue())
                .minOrderAmount(request.minOrderAmount())
                .maxUsage(request.maxUsage())
                .usedCount(0)
                .expiresAt(request.expiresAt())
                .active(true)
                .build();
        return CouponResponse.from(couponRepository.save(coupon));
    }

    public List<CouponResponse> getAllCoupons() {
        return couponRepository.findAll().stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(CouponResponse::from)
                .toList();
    }

    public CouponValidateResponse validate(String code, int orderAmount) {
        Coupon coupon = couponRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> new BusinessException(ErrorCode.COUPON_NOT_FOUND));

        if (!coupon.isActive()) throw new BusinessException(ErrorCode.COUPON_INACTIVE);
        if (LocalDateTime.now().isAfter(coupon.getExpiresAt())) throw new BusinessException(ErrorCode.COUPON_EXPIRED);
        if (coupon.getUsedCount() >= coupon.getMaxUsage()) throw new BusinessException(ErrorCode.COUPON_EXHAUSTED);
        if (orderAmount < coupon.getMinOrderAmount()) throw new BusinessException(ErrorCode.COUPON_MIN_ORDER_NOT_MET);

        int discountAmount = coupon.calculateDiscount(orderAmount);
        return new CouponValidateResponse(
                coupon.getCode(),
                coupon.getDiscountType().name(),
                coupon.getDiscountValue(),
                discountAmount,
                coupon.getMinOrderAmount()
        );
    }

    public Coupon getAndValidateCoupon(String code, int orderAmount) {
        Coupon coupon = couponRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> new BusinessException(ErrorCode.COUPON_NOT_FOUND));
        if (!coupon.isValid(orderAmount)) {
            if (!coupon.isActive()) throw new BusinessException(ErrorCode.COUPON_INACTIVE);
            if (LocalDateTime.now().isAfter(coupon.getExpiresAt())) throw new BusinessException(ErrorCode.COUPON_EXPIRED);
            if (coupon.getUsedCount() >= coupon.getMaxUsage()) throw new BusinessException(ErrorCode.COUPON_EXHAUSTED);
            throw new BusinessException(ErrorCode.COUPON_MIN_ORDER_NOT_MET);
        }
        return coupon;
    }

    @Transactional
    public void deactivateCoupon(Long couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COUPON_NOT_FOUND));
        coupon.deactivate();
    }
}
