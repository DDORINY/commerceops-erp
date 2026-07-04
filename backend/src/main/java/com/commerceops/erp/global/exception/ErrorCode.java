package com.commerceops.erp.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 공통
    INVALID_INPUT_VALUE(400, "COMMON_001", "잘못된 입력 값입니다."),
    INVALID_TYPE_VALUE(400, "COMMON_002", "잘못된 타입입니다."),
    METHOD_NOT_ALLOWED(405, "COMMON_003", "허용되지 않은 HTTP 메서드입니다."),
    INTERNAL_SERVER_ERROR(500, "COMMON_004", "서버 오류가 발생했습니다."),
    NOT_FOUND(404, "COMMON_005", "요청한 리소스를 찾을 수 없습니다."),
    UNAUTHORIZED(401, "COMMON_006", "인증이 필요합니다."),
    FORBIDDEN(403, "COMMON_007", "접근 권한이 없습니다."),

    // 인증/회원 (auth, user)
    USER_NOT_FOUND(404, "USER_001", "존재하지 않는 회원입니다."),
    DUPLICATE_EMAIL(409, "USER_002", "이미 사용 중인 이메일입니다."),
    INVALID_PASSWORD(400, "USER_003", "비밀번호가 올바르지 않습니다."),
    INVALID_TOKEN(401, "AUTH_001", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(401, "AUTH_002", "만료된 토큰입니다."),

    // 카테고리 (category)
    CATEGORY_NOT_FOUND(404, "CATEGORY_001", "존재하지 않는 카테고리입니다."),

    // 상품 (product)
    PRODUCT_NOT_FOUND(404, "PRODUCT_001", "존재하지 않는 상품입니다."),
    PRODUCT_SOLD_OUT(400, "PRODUCT_002", "품절된 상품입니다."),
    INSUFFICIENT_STOCK(400, "PRODUCT_003", "재고가 부족합니다."),

    // 주문 (order)
    ORDER_NOT_FOUND(404, "ORDER_001", "존재하지 않는 주문입니다."),
    ORDER_CANCEL_NOT_ALLOWED(400, "ORDER_002", "취소할 수 없는 주문 상태입니다."),
    ORDER_ACCESS_DENIED(403, "ORDER_003", "본인 주문만 접근할 수 있습니다."),
    EMPTY_CART(400, "ORDER_004", "주문할 장바구니 항목이 없습니다."),
    INVALID_ORDER_STATUS(400, "ORDER_005", "유효하지 않은 주문 상태입니다."),

    // 장바구니 (cart)
    CART_ITEM_NOT_FOUND(404, "CART_001", "장바구니 항목을 찾을 수 없습니다."),
    INVALID_QUANTITY(400, "CART_002", "수량은 1 이상이어야 합니다."),
    OUT_OF_STOCK(400, "CART_003", "재고가 부족합니다."),
    PRODUCT_NOT_AVAILABLE(400, "PRODUCT_004", "구매 불가능한 상품입니다."),

    // 재고 (inventory)
    INVENTORY_NOT_FOUND(404, "INVENTORY_001", "재고 정보를 찾을 수 없습니다."),

    // 결제 (payment)
    PAYMENT_NOT_FOUND(404, "PAYMENT_001", "결제 정보를 찾을 수 없습니다."),
    ALREADY_PAID(409, "PAYMENT_002", "이미 결제된 주문입니다."),
    PAYMENT_FAILED(400, "PAYMENT_003", "결제에 실패했습니다."),

    // 배송 (shipment)
    SHIPMENT_NOT_FOUND(404, "SHIPMENT_001", "배송 정보를 찾을 수 없습니다."),
    SHIPMENT_ALREADY_DELIVERED(400, "SHIPMENT_002", "이미 배송 완료된 건입니다."),
    SHIPMENT_NOT_IN_TRANSIT(400, "SHIPMENT_003", "배송 중인 상태에서만 완료 처리할 수 있습니다."),
    SHIPMENT_CANCELLED(400, "SHIPMENT_004", "취소된 배송 건은 처리할 수 없습니다."),

    // 반품 (returns)
    RETURN_NOT_FOUND(404, "RETURN_001", "반품 요청을 찾을 수 없습니다."),
    RETURN_ALREADY_REQUESTED(409, "RETURN_002", "이미 반품 요청된 주문입니다."),
    RETURN_ALREADY_PROCESSED(400, "RETURN_003", "이미 처리된 반품 요청입니다."),
    ORDER_NOT_RETURNABLE(400, "RETURN_004", "반품 신청이 불가능한 주문 상태입니다."),

    // 문의 (inquiry)
    INQUIRY_NOT_FOUND(404, "INQUIRY_001", "문의를 찾을 수 없습니다."),
    INQUIRY_ALREADY_ANSWERED(400, "INQUIRY_002", "이미 답변된 문의입니다."),
    INQUIRY_ACCESS_DENIED(403, "INQUIRY_003", "본인 문의만 접근할 수 있습니다."),

    // 창고 (warehouse)
    WAREHOUSE_NOT_FOUND(404, "WAREHOUSE_001", "창고를 찾을 수 없습니다."),
    DUPLICATE_WAREHOUSE_CODE(409, "WAREHOUSE_002", "이미 사용 중인 창고 코드입니다."),
    WAREHOUSE_INACTIVE(400, "WAREHOUSE_003", "비활성 창고는 사용할 수 없습니다."),
    WAREHOUSE_STOCK_NOT_FOUND(404, "WAREHOUSE_004", "창고 재고를 찾을 수 없습니다."),
    INSUFFICIENT_WAREHOUSE_STOCK(400, "WAREHOUSE_005", "출발 창고의 재고가 부족합니다."),
    WAREHOUSE_ALLOCATION_EXCEEDS_TOTAL(400, "WAREHOUSE_006", "미배정 재고보다 많이 배치할 수 없습니다."),
    SAME_WAREHOUSE_TRANSFER(400, "WAREHOUSE_007", "같은 창고로 재고를 이동할 수 없습니다."),
    STOCK_TRANSFER_NOT_FOUND(404, "WAREHOUSE_008", "재고 이동 요청을 찾을 수 없습니다."),
    STOCK_TRANSFER_ALREADY_COMPLETED(409, "WAREHOUSE_009", "이미 완료된 재고 이동입니다."),

    // 쿠폰 (coupon)
    COUPON_NOT_FOUND(404, "COUPON_001", "존재하지 않는 쿠폰 코드입니다."),
    COUPON_EXPIRED(400, "COUPON_002", "만료된 쿠폰입니다."),
    COUPON_EXHAUSTED(400, "COUPON_003", "쿠폰 사용 횟수가 초과되었습니다."),
    COUPON_MIN_ORDER_NOT_MET(400, "COUPON_004", "최소 주문 금액을 충족하지 않습니다."),
    COUPON_INACTIVE(400, "COUPON_005", "비활성화된 쿠폰입니다."),
    DUPLICATE_COUPON_CODE(409, "COUPON_006", "이미 존재하는 쿠폰 코드입니다."),

    // 리뷰 (review)
    REVIEW_NOT_FOUND(404, "REVIEW_001", "리뷰를 찾을 수 없습니다."),
    REVIEW_ALREADY_EXISTS(409, "REVIEW_002", "이미 작성한 리뷰입니다."),
    REVIEW_ACCESS_DENIED(403, "REVIEW_003", "본인 리뷰만 삭제할 수 있습니다."),
    ORDER_NOT_COMPLETED(400, "REVIEW_004", "배송 완료된 주문에만 리뷰를 작성할 수 있습니다."),
    ORDER_ITEM_NOT_IN_ORDER(400, "REVIEW_005", "해당 주문의 상품이 아닙니다.");

    private final int status;
    private final String code;
    private final String message;
}
