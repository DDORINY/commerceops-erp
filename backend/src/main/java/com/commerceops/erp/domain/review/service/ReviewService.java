package com.commerceops.erp.domain.review.service;

import com.commerceops.erp.domain.order.entity.Order;
import com.commerceops.erp.domain.order.entity.OrderItem;
import com.commerceops.erp.domain.order.enums.OrderStatus;
import com.commerceops.erp.domain.order.repository.OrderItemRepository;
import com.commerceops.erp.domain.order.repository.OrderRepository;
import com.commerceops.erp.domain.review.dto.ReviewCreateRequest;
import com.commerceops.erp.domain.review.dto.ReviewResponse;
import com.commerceops.erp.domain.review.entity.Review;
import com.commerceops.erp.domain.review.repository.ReviewRepository;
import com.commerceops.erp.domain.user.entity.User;
import com.commerceops.erp.global.exception.BusinessException;
import com.commerceops.erp.global.exception.ErrorCode;
import com.commerceops.erp.global.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    @Transactional
    public ReviewResponse createReview(Long orderId, Long orderItemId, User user, ReviewCreateRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.ORDER_ACCESS_DENIED);
        }
        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.ORDER_NOT_COMPLETED);
        }

        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if (!orderItem.getOrder().getId().equals(orderId)) {
            throw new BusinessException(ErrorCode.ORDER_ITEM_NOT_IN_ORDER);
        }
        if (reviewRepository.existsByOrderItemId(orderItemId)) {
            throw new BusinessException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }

        Review review = Review.builder()
                .product(orderItem.getProduct())
                .user(user)
                .orderItemId(orderItemId)
                .rating(request.rating())
                .content(request.content())
                .build();

        return ReviewResponse.from(reviewRepository.save(review));
    }

    public PageResponse<ReviewResponse> getProductReviews(Long productId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return PageResponse.from(
                reviewRepository.findByProductIdOrderByCreatedAtDesc(productId, pageable)
                        .map(ReviewResponse::from)
        );
    }

    public List<ReviewResponse> getMyReviews(User user) {
        return reviewRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(ReviewResponse::from)
                .toList();
    }

    public PageResponse<ReviewResponse> getAdminReviews(Integer rating, String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return PageResponse.from(
                reviewRepository.findAllForAdmin(rating, keyword, pageable)
                        .map(ReviewResponse::from)
        );
    }

    @Transactional
    public void deleteReview(Long reviewId, User user) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));

        if (!review.getUser().getId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.REVIEW_ACCESS_DENIED);
        }
        reviewRepository.delete(review);
    }

    @Transactional
    public void adminDeleteReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));
        reviewRepository.delete(review);
    }
}
