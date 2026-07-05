package com.commerceops.erp.domain.cart.service;

import com.commerceops.erp.domain.cart.dto.*;
import com.commerceops.erp.domain.cart.entity.Cart;
import com.commerceops.erp.domain.cart.repository.CartRepository;
import com.commerceops.erp.domain.product.entity.Product;
import com.commerceops.erp.domain.product.repository.ProductRepository;
import com.commerceops.erp.domain.user.entity.User;
import com.commerceops.erp.global.exception.BusinessException;
import com.commerceops.erp.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final ObjectMapper objectMapper;

    public CartResponse getCart(User user) {
        List<CartItemResponse> items = cartRepository.findAllByUserWithProduct(user)
                .stream()
                .map(CartItemResponse::from)
                .toList();
        return CartResponse.of(items);
    }

    @Transactional
    public CartAddResponse addToCart(User user, CartAddRequest request) {
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        if (!product.isPurchasable(java.time.LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_AVAILABLE);
        }

        String selectedOptionsJson = serializeOptions(request.selectedOptions());

        Cart cart = cartRepository.findByUserAndProductAndSelectedOptions(user, product, selectedOptionsJson)
                .map(existing -> {
                    int newQty = existing.getQuantity() + request.quantity();
                    validateStock(product, newQty);
                    existing.increaseQuantity(request.quantity());
                    return existing;
                })
                .orElseGet(() -> {
                    validateStock(product, request.quantity());
                    return cartRepository.save(Cart.builder()
                            .user(user)
                            .product(product)
                            .quantity(request.quantity())
                            .selectedOptions(selectedOptionsJson)
                            .build());
                });

        return CartAddResponse.from(cart);
    }

    private String serializeOptions(Map<String, String> options) {
        if (options == null || options.isEmpty()) return null;
        try {
            return objectMapper.writeValueAsString(new TreeMap<>(options));
        } catch (Exception e) {
            return null;
        }
    }

    @Transactional
    public CartUpdateResponse updateCartItem(User user, Long cartId, CartUpdateRequest request) {
        Cart cart = findCartAndVerifyOwner(cartId, user);

        validateStock(cart.getProduct(), request.quantity());
        cart.updateQuantity(request.quantity());

        return CartUpdateResponse.from(cart);
    }

    @Transactional
    public void removeFromCart(User user, Long cartId) {
        Cart cart = findCartAndVerifyOwner(cartId, user);
        cartRepository.delete(cart);
    }

    private Cart findCartAndVerifyOwner(Long cartId, User user) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND));
        if (!cart.getUser().getId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return cart;
    }

    private void validateStock(Product product, int requestedQty) {
        if (!product.isPurchasable(java.time.LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_AVAILABLE);
        }
        if (requestedQty > product.getStockQuantity()) {
            throw new BusinessException(ErrorCode.OUT_OF_STOCK);
        }
    }
}
