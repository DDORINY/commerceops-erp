package com.commerceops.erp.domain.product.service;

import com.commerceops.erp.domain.product.dto.ProductDetailBlockRequest;
import com.commerceops.erp.domain.product.dto.ProductDetailBlockResponse;
import com.commerceops.erp.domain.product.entity.Product;
import com.commerceops.erp.domain.product.entity.ProductDetailBlock;
import com.commerceops.erp.domain.product.enums.ProductDetailBlockType;
import com.commerceops.erp.domain.product.enums.ProductStatus;
import com.commerceops.erp.domain.product.repository.ProductDetailBlockRepository;
import com.commerceops.erp.domain.product.repository.ProductRepository;
import com.commerceops.erp.global.exception.BusinessException;
import com.commerceops.erp.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductDetailBlockService {

    private static final int MAX_TEXT_LENGTH = 10000;

    private final ProductRepository productRepository;
    private final ProductDetailBlockRepository productDetailBlockRepository;

    public List<ProductDetailBlockResponse> getAdminBlocks(Long productId) {
        ensureProductExists(productId, true);
        return productDetailBlockRepository.findByProductIdOrderBySortOrderAscIdAsc(productId)
                .stream()
                .map(ProductDetailBlockResponse::from)
                .toList();
    }

    public List<ProductDetailBlockResponse> getVisibleBlocks(Long productId) {
        return productDetailBlockRepository.findByProductIdAndVisibleTrueOrderBySortOrderAscIdAsc(productId)
                .stream()
                .map(ProductDetailBlockResponse::from)
                .toList();
    }

    @Transactional
    public List<ProductDetailBlockResponse> replaceAdminBlocks(Long productId, List<ProductDetailBlockRequest> requests) {
        Product product = ensureProductExists(productId, true);
        List<ProductDetailBlockRequest> safeRequests = requests != null ? requests : List.of();
        safeRequests.forEach(this::validateBlock);

        productDetailBlockRepository.deleteByProductId(productId);

        AtomicInteger fallbackOrder = new AtomicInteger(0);
        List<ProductDetailBlock> blocks = safeRequests.stream()
                .sorted(Comparator.comparing(
                        request -> request.sortOrder() != null ? request.sortOrder() : Integer.MAX_VALUE
                ))
                .map(request -> ProductDetailBlock.builder()
                        .product(product)
                        .blockType(request.blockType())
                        .title(normalizeText(request.title()))
                        .content(normalizeText(request.content()))
                        .imageUrl(normalizeText(request.imageUrl()))
                        .specJson(normalizeText(request.specJson()))
                        .sortOrder(fallbackOrder.getAndIncrement())
                        .visible(request.visible() == null || request.visible())
                        .build())
                .toList();

        return productDetailBlockRepository.saveAll(blocks)
                .stream()
                .map(ProductDetailBlockResponse::from)
                .toList();
    }

    private Product ensureProductExists(Long productId, boolean includeHidden) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        if (product.getStatus() == ProductStatus.DELETED ||
                (!includeHidden && product.getStatus() == ProductStatus.HIDDEN)) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        return product;
    }

    private void validateBlock(ProductDetailBlockRequest request) {
        if (request == null || request.blockType() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        validateLength(request.title(), 200);
        validateLength(request.content(), MAX_TEXT_LENGTH);
        validateLength(request.imageUrl(), 500);
        validateLength(request.specJson(), MAX_TEXT_LENGTH);

        if (request.blockType() == ProductDetailBlockType.IMAGE && isBlank(request.imageUrl())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (request.blockType() == ProductDetailBlockType.SPEC_TABLE && isBlank(request.specJson())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private void validateLength(String value, int maxLength) {
        if (value != null && value.length() > maxLength) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? "" : trimmed;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
