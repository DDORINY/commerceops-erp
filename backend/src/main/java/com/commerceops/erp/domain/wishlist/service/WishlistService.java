package com.commerceops.erp.domain.wishlist.service;

import com.commerceops.erp.domain.product.entity.Product;
import com.commerceops.erp.domain.product.repository.ProductRepository;
import com.commerceops.erp.domain.user.entity.User;
import com.commerceops.erp.domain.wishlist.dto.WishlistItemResponse;
import com.commerceops.erp.domain.wishlist.dto.WishlistToggleResponse;
import com.commerceops.erp.domain.wishlist.entity.Wishlist;
import com.commerceops.erp.domain.wishlist.repository.WishlistRepository;
import com.commerceops.erp.global.exception.BusinessException;
import com.commerceops.erp.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;

    @Transactional
    public WishlistToggleResponse toggle(Long productId, User user) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        boolean exists = wishlistRepository.existsByUserIdAndProductId(user.getId(), productId);
        if (exists) {
            wishlistRepository.deleteByUserIdAndProductId(user.getId(), productId);
            return new WishlistToggleResponse(productId, false);
        } else {
            wishlistRepository.save(Wishlist.builder()
                    .user(user)
                    .product(product)
                    .build());
            return new WishlistToggleResponse(productId, true);
        }
    }

    public List<WishlistItemResponse> getMyWishlist(User user) {
        return wishlistRepository.findByUserIdWithProduct(user.getId())
                .stream()
                .map(WishlistItemResponse::from)
                .toList();
    }

    public boolean isLiked(Long productId, User user) {
        return wishlistRepository.existsByUserIdAndProductId(user.getId(), productId);
    }
}
