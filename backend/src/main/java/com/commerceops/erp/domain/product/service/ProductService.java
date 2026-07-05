package com.commerceops.erp.domain.product.service;

import com.commerceops.erp.domain.category.entity.Category;
import com.commerceops.erp.domain.category.repository.CategoryRepository;
import com.commerceops.erp.domain.audit.enums.AuditActionType;
import com.commerceops.erp.domain.audit.service.AuditLogService;
import com.commerceops.erp.domain.product.dto.*;
import com.commerceops.erp.domain.product.entity.Product;
import com.commerceops.erp.domain.product.entity.ProductOperationNote;
import com.commerceops.erp.domain.product.entity.ProductStatusHistory;
import com.commerceops.erp.domain.product.enums.ProductDisplayStatus;
import com.commerceops.erp.domain.product.enums.ProductSalesStatus;
import com.commerceops.erp.domain.product.enums.ProductStatus;
import com.commerceops.erp.domain.product.repository.ProductOperationNoteRepository;
import com.commerceops.erp.domain.product.repository.ProductRepository;
import com.commerceops.erp.domain.product.repository.ProductStatusHistoryRepository;
import com.commerceops.erp.domain.user.entity.User;
import com.commerceops.erp.global.exception.BusinessException;
import com.commerceops.erp.global.exception.ErrorCode;
import com.commerceops.erp.global.response.PageResponse;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductDetailBlockService productDetailBlockService;
    private final ProductStatusHistoryRepository productStatusHistoryRepository;
    private final ProductOperationNoteRepository productOperationNoteRepository;
    private final AuditLogService auditLogService;

    public PageResponse<ProductListResponse> getProducts(Long categoryId, String keyword,
                                                          String sort, Integer minPrice, Integer maxPrice,
                                                          boolean inStock, int page, int size) {
        Sort sortOrder = switch (sort != null ? sort : "") {
            case "priceAsc" -> Sort.by("price").ascending();
            case "priceDesc" -> Sort.by("price").descending();
            default -> Sort.by("createdAt").descending();
        };
        Pageable pageable = PageRequest.of(page, size, sortOrder);

        Specification<Product> spec = buildPublicSpec(categoryId, keyword, minPrice, maxPrice, inStock);
        Page<Product> products = productRepository.findAll(spec, pageable);

        return PageResponse.from(products.map(ProductListResponse::from));
    }

    public ProductResponse getProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        if (!product.isPubliclyVisible()) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        return ProductResponse.from(product, productDetailBlockService.getVisibleBlocks(productId));
    }

    public PageResponse<AdminProductListResponse> getAdminProducts(ProductStatus status,
                                                                    ProductSalesStatus salesStatus,
                                                                    ProductDisplayStatus displayStatus,
                                                                    Long categoryId,
                                                                    String stockStatus,
                                                                    Boolean lowStockOnly,
                                                                    String salePeriodStatus,
                                                                    String keyword,
                                                                    int page,
                                                                    int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Product> products = productRepository.findAll(buildAdminSpec(status, salesStatus, displayStatus,
                categoryId, stockStatus, lowStockOnly, salePeriodStatus, keyword), pageable);
        return PageResponse.from(products.map(AdminProductListResponse::from));
    }

    public AdminProductResponse getAdminProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        if (product.getDeletedAt() != null || product.getStatus() == ProductStatus.DELETED) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        return AdminProductResponse.from(product);
    }

    @Transactional
    public AdminProductResponse createProduct(ProductCreateRequest request) {
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));
        validateCatalogMaster(request.price(), request.originalPrice(), request.discountPrice(),
                request.purchasePrice(), request.saleStartAt(), request.saleEndAt(), request.safetyStockQuantity());

        Product product = Product.builder()
                .category(category)
                .name(request.name())
                .description(request.description())
                .price(request.price())
                .productCode(normalizeText(request.productCode()))
                .brand(normalizeText(request.brand()))
                .manufacturer(normalizeText(request.manufacturer()))
                .modelName(normalizeText(request.modelName()))
                .origin(normalizeText(request.origin()))
                .originalPrice(request.originalPrice())
                .discountPrice(request.discountPrice())
                .purchasePrice(request.purchasePrice())
                .searchKeywords(normalizeText(request.searchKeywords()))
                .tags(normalizeText(request.tags()))
                .saleStartAt(request.saleStartAt())
                .saleEndAt(request.saleEndAt())
                .deliveryInfo(normalizeText(request.deliveryInfo()))
                .seoTitle(normalizeText(request.seoTitle()))
                .seoDescription(normalizeText(request.seoDescription()))
                .seoKeywords(normalizeText(request.seoKeywords()))
                .stockQuantity(request.stockQuantity())
                .imageUrl(request.imageUrl())
                .status(request.status() != null ? request.status() :
                        legacyStatusFrom(request.salesStatus(), request.displayStatus(), request.stockQuantity()))
                .salesStatus(request.salesStatus() != null ? request.salesStatus() : ProductSalesStatus.ON_SALE)
                .displayStatus(request.displayStatus() != null ? request.displayStatus() : ProductDisplayStatus.VISIBLE)
                .safetyStockQuantity(request.safetyStockQuantity() != null ? request.safetyStockQuantity() : 5)
                .options(request.options())
                .build();
        product.updateOperationStatus(product.getSalesStatus(), product.getDisplayStatus(), product.getSafetyStockQuantity());

        return AdminProductResponse.from(productRepository.save(product));
    }

    @Transactional
    public AdminProductResponse updateProduct(Long productId, ProductUpdateRequest request, User actor) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        Category category = null;
        if (request.categoryId() != null) {
            category = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));
        }

        validateCatalogMaster(
                request.price() != null ? request.price() : product.getPrice(),
                request.originalPrice() != null ? request.originalPrice() : product.getOriginalPrice(),
                request.discountPrice() != null ? request.discountPrice() : product.getDiscountPrice(),
                request.purchasePrice() != null ? request.purchasePrice() : product.getPurchasePrice(),
                request.saleStartAt() != null ? request.saleStartAt() : product.getSaleStartAt(),
                request.saleEndAt() != null ? request.saleEndAt() : product.getSaleEndAt(),
                request.safetyStockQuantity() != null ? request.safetyStockQuantity() : product.getSafetyStockQuantity()
        );

        ProductSalesStatus previousSalesStatus = product.getSalesStatus();
        ProductDisplayStatus previousDisplayStatus = product.getDisplayStatus();

        product.update(category, request.name(), request.description(),
                request.price(), request.stockQuantity(), request.imageUrl(),
                request.status(), request.options(),
                normalizeText(request.productCode()), normalizeText(request.brand()),
                normalizeText(request.manufacturer()), normalizeText(request.modelName()),
                normalizeText(request.origin()), request.originalPrice(),
                request.discountPrice(), request.purchasePrice(),
                normalizeText(request.searchKeywords()), normalizeText(request.tags()),
                request.saleStartAt(), request.saleEndAt(), normalizeText(request.deliveryInfo()),
                normalizeText(request.seoTitle()), normalizeText(request.seoDescription()),
                normalizeText(request.seoKeywords()), request.salesStatus(),
                request.displayStatus(), request.safetyStockQuantity());

        recordStatusChangeIfChanged(product, actor, previousSalesStatus, previousDisplayStatus, "Product updated.");

        return AdminProductResponse.from(product);
    }

    @Transactional
    public AdminProductResponse updateProductStatus(Long productId, ProductStatusUpdateRequest request, User actor) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        validateOperationStatus(request.salesStatus(), request.displayStatus(), request.safetyStockQuantity());
        ProductSalesStatus previousSalesStatus = product.getSalesStatus();
        ProductDisplayStatus previousDisplayStatus = product.getDisplayStatus();
        product.updateOperationStatus(request.salesStatus(), request.displayStatus(), request.safetyStockQuantity());
        recordStatusChangeIfChanged(product, actor, previousSalesStatus, previousDisplayStatus, request.reason());
        return AdminProductResponse.from(product);
    }

    @Transactional
    public ProductBulkStatusUpdateResponse bulkUpdateProductStatus(ProductBulkStatusUpdateRequest request, User actor) {
        validateOperationStatus(request.salesStatus(), request.displayStatus(), null);
        List<Product> products = productRepository.findAllById(request.productIds());
        if (products.size() != request.productIds().size()) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        if (request.salesStatus() == null && request.displayStatus() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        List<AdminProductListResponse> updated = products.stream()
                .map(product -> {
                    ProductSalesStatus previousSalesStatus = product.getSalesStatus();
                    ProductDisplayStatus previousDisplayStatus = product.getDisplayStatus();
                    product.updateOperationStatus(request.salesStatus(), request.displayStatus(), null);
                    recordStatusChangeIfChanged(product, actor, previousSalesStatus, previousDisplayStatus, request.reason());
                    return AdminProductListResponse.from(product);
                })
                .toList();

        auditLogService.record(
                actor,
                AuditActionType.PRODUCT_BULK_STATUS_UPDATE,
                "PRODUCT",
                0L,
                null,
                statusSummary(request.salesStatus(), request.displayStatus()),
                "Bulk product status update: " + updated.size() + " products"
        );

        return new ProductBulkStatusUpdateResponse(updated.size(), updated);
    }

    public List<ProductStatusHistoryResponse> getProductStatusHistory(Long productId, int limit) {
        ensureAdminProductExists(productId);
        int size = Math.max(1, Math.min(limit, 50));
        return productStatusHistoryRepository.findByProductIdOrderByCreatedAtDesc(productId, PageRequest.of(0, size))
                .stream()
                .map(ProductStatusHistoryResponse::from)
                .toList();
    }

    public List<ProductOperationNoteResponse> getProductOperationNotes(Long productId, int limit) {
        ensureAdminProductExists(productId);
        int size = Math.max(1, Math.min(limit, 50));
        return productOperationNoteRepository.findByProductIdOrderByCreatedAtDesc(productId, PageRequest.of(0, size))
                .stream()
                .map(ProductOperationNoteResponse::from)
                .toList();
    }

    @Transactional
    public ProductOperationNoteResponse createProductOperationNote(Long productId, ProductOperationNoteRequest request, User actor) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
        if (product.getDeletedAt() != null || product.getStatus() == ProductStatus.DELETED) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        ProductOperationNote note = productOperationNoteRepository.save(ProductOperationNote.builder()
                .product(product)
                .writerUserId(actor.getId())
                .writerEmail(actor.getEmail())
                .content(request.content().trim())
                .build());

        auditLogService.record(
                actor,
                AuditActionType.PRODUCT_OPERATION_NOTE_CREATE,
                "PRODUCT",
                productId,
                null,
                null,
                "Product operation note created."
        );
        return ProductOperationNoteResponse.from(note);
    }

    @Transactional
    public void deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
        product.softDelete();
    }

    private Specification<Product> buildPublicSpec(Long categoryId, String keyword,
                                                     Integer minPrice, Integer maxPrice, boolean inStock) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.notEqual(root.get("status"), ProductStatus.DELETED));
            predicates.add(cb.isNull(root.get("deletedAt")));
            predicates.add(cb.equal(root.get("displayStatus"), ProductDisplayStatus.VISIBLE));
            predicates.add(root.get("salesStatus").in(List.of(
                    ProductSalesStatus.ON_SALE,
                    ProductSalesStatus.PAUSED,
                    ProductSalesStatus.SOLD_OUT
            )));

            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }
            if (keyword != null && !keyword.isBlank()) {
                String pattern = "%" + keyword.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), pattern),
                        cb.like(cb.lower(root.get("description")), pattern),
                        cb.like(cb.lower(root.get("brand")), pattern),
                        cb.like(cb.lower(root.get("modelName")), pattern),
                        cb.like(cb.lower(root.get("searchKeywords")), pattern),
                        cb.like(cb.lower(root.get("tags")), pattern)
                ));
            }
            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }
            if (inStock) {
                predicates.add(cb.greaterThan(root.get("stockQuantity"), 0));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Specification<Product> buildAdminSpec(ProductStatus status,
                                                  ProductSalesStatus salesStatus,
                                                  ProductDisplayStatus displayStatus,
                                                  Long categoryId,
                                                  String stockStatus,
                                                  Boolean lowStockOnly,
                                                  String salePeriodStatus,
                                                  String keyword) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isNull(root.get("deletedAt")));

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (salesStatus != null) {
                predicates.add(cb.equal(root.get("salesStatus"), salesStatus));
            }
            if (displayStatus != null) {
                predicates.add(cb.equal(root.get("displayStatus"), displayStatus));
            }
            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }
            if (Boolean.TRUE.equals(lowStockOnly)) {
                predicates.add(cb.greaterThan(root.get("stockQuantity"), 0));
                predicates.add(cb.lessThanOrEqualTo(root.get("stockQuantity"), root.get("safetyStockQuantity")));
            }
            if (stockStatus != null && !stockStatus.isBlank() && !"ALL".equalsIgnoreCase(stockStatus)) {
                String normalizedStockStatus = stockStatus.toUpperCase();
                if ("SOLD_OUT".equals(normalizedStockStatus)) {
                    predicates.add(cb.or(
                            cb.lessThanOrEqualTo(root.get("stockQuantity"), 0),
                            cb.equal(root.get("salesStatus"), ProductSalesStatus.SOLD_OUT)
                    ));
                } else if ("LOW_STOCK".equals(normalizedStockStatus)) {
                    predicates.add(cb.greaterThan(root.get("stockQuantity"), 0));
                    predicates.add(cb.lessThanOrEqualTo(root.get("stockQuantity"), root.get("safetyStockQuantity")));
                } else if ("IN_STOCK".equals(normalizedStockStatus)) {
                    predicates.add(cb.greaterThan(root.get("stockQuantity"), root.get("safetyStockQuantity")));
                    predicates.add(cb.notEqual(root.get("salesStatus"), ProductSalesStatus.SOLD_OUT));
                }
            }
            if (salePeriodStatus != null && !salePeriodStatus.isBlank() && !"ALL".equalsIgnoreCase(salePeriodStatus)) {
                java.time.LocalDateTime now = java.time.LocalDateTime.now();
                String normalizedSalePeriodStatus = salePeriodStatus.toUpperCase();
                if ("ACTIVE".equals(normalizedSalePeriodStatus)) {
                    predicates.add(cb.or(cb.isNull(root.get("saleStartAt")), cb.lessThanOrEqualTo(root.get("saleStartAt"), now)));
                    predicates.add(cb.or(cb.isNull(root.get("saleEndAt")), cb.greaterThanOrEqualTo(root.get("saleEndAt"), now)));
                } else if ("UPCOMING".equals(normalizedSalePeriodStatus)) {
                    predicates.add(cb.greaterThan(root.get("saleStartAt"), now));
                } else if ("ENDED".equals(normalizedSalePeriodStatus)) {
                    predicates.add(cb.lessThan(root.get("saleEndAt"), now));
                }
            }
            if (keyword != null && !keyword.isBlank()) {
                String pattern = "%" + keyword.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), pattern),
                        cb.like(cb.lower(root.get("description")), pattern),
                        cb.like(cb.lower(root.get("productCode")), pattern),
                        cb.like(cb.lower(root.get("brand")), pattern),
                        cb.like(cb.lower(root.get("manufacturer")), pattern),
                        cb.like(cb.lower(root.get("modelName")), pattern),
                        cb.like(cb.lower(root.get("searchKeywords")), pattern),
                        cb.like(cb.lower(root.get("tags")), pattern)
                ));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private void validateCatalogMaster(Integer price, Integer originalPrice, Integer discountPrice,
                                       Integer purchasePrice, java.time.LocalDateTime saleStartAt,
                                       java.time.LocalDateTime saleEndAt, Integer safetyStockQuantity) {
        if (price != null && price < 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (originalPrice != null && originalPrice < 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (discountPrice != null && discountPrice < 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (purchasePrice != null && purchasePrice < 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (originalPrice != null && price != null && originalPrice < price) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (discountPrice != null && price != null && discountPrice > price) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (saleStartAt != null && saleEndAt != null && saleEndAt.isBefore(saleStartAt)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (safetyStockQuantity != null && safetyStockQuantity < 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private void validateOperationStatus(ProductSalesStatus salesStatus,
                                         ProductDisplayStatus displayStatus,
                                         Integer safetyStockQuantity) {
        if (safetyStockQuantity != null && safetyStockQuantity < 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private ProductStatus legacyStatusFrom(ProductSalesStatus salesStatus,
                                           ProductDisplayStatus displayStatus,
                                           Integer stockQuantity) {
        if (displayStatus == ProductDisplayStatus.HIDDEN
                || salesStatus == ProductSalesStatus.DRAFT
                || salesStatus == ProductSalesStatus.PAUSED
                || salesStatus == ProductSalesStatus.DISCONTINUED) {
            return ProductStatus.HIDDEN;
        }
        if (salesStatus == ProductSalesStatus.SOLD_OUT || (stockQuantity != null && stockQuantity <= 0)) {
            return ProductStatus.SOLD_OUT;
        }
        return ProductStatus.ON_SALE;
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? "" : trimmed;
    }

    private void recordStatusChangeIfChanged(Product product, User actor,
                                             ProductSalesStatus previousSalesStatus,
                                             ProductDisplayStatus previousDisplayStatus,
                                             String reason) {
        boolean changed = previousSalesStatus != product.getSalesStatus()
                || previousDisplayStatus != product.getDisplayStatus();
        if (!changed) {
            return;
        }

        productStatusHistoryRepository.save(ProductStatusHistory.builder()
                .product(product)
                .changedByUserId(actor.getId())
                .changedByEmail(actor.getEmail())
                .previousSalesStatus(previousSalesStatus)
                .newSalesStatus(product.getSalesStatus())
                .previousDisplayStatus(previousDisplayStatus)
                .newDisplayStatus(product.getDisplayStatus())
                .reason(normalizeReason(reason))
                .build());

        auditLogService.record(
                actor,
                AuditActionType.PRODUCT_STATUS_UPDATE,
                "PRODUCT",
                product.getId(),
                statusSummary(previousSalesStatus, previousDisplayStatus),
                statusSummary(product.getSalesStatus(), product.getDisplayStatus()),
                normalizeReason(reason)
        );
    }

    private String statusSummary(ProductSalesStatus salesStatus, ProductDisplayStatus displayStatus) {
        return "salesStatus=" + (salesStatus != null ? salesStatus.name() : "-")
                + ", displayStatus=" + (displayStatus != null ? displayStatus.name() : "-");
    }

    private String normalizeReason(String reason) {
        if (reason == null || reason.isBlank()) {
            return "No reason provided.";
        }
        return reason.trim();
    }

    private void ensureAdminProductExists(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
        if (product.getDeletedAt() != null || product.getStatus() == ProductStatus.DELETED) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }
    }
}
