package com.commerceops.erp.global.config;

import com.commerceops.erp.domain.category.entity.Category;
import com.commerceops.erp.domain.category.repository.CategoryRepository;
import com.commerceops.erp.domain.product.entity.Product;
import com.commerceops.erp.domain.product.enums.ProductStatus;
import com.commerceops.erp.domain.product.repository.ProductRepository;
import com.commerceops.erp.domain.user.entity.User;
import com.commerceops.erp.domain.user.repository.UserRepository;
import com.commerceops.erp.domain.warehouse.entity.Warehouse;
import com.commerceops.erp.domain.warehouse.entity.WarehouseStock;
import com.commerceops.erp.domain.warehouse.repository.WarehouseRepository;
import com.commerceops.erp.domain.warehouse.repository.WarehouseStockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile({"local", "dev", "test"})
@ConditionalOnProperty(name = "app.initializer.enabled", havingValue = "true")
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final WarehouseStockRepository warehouseStockRepository;

    @Value("${app.initializer.admin-email:}")
    private String initialAdminEmail;

    @Value("${app.initializer.admin-password:}")
    private String initialAdminPassword;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        createAdminIfNotExists();
        createInitialCategories();
        createInitialProducts();
        createDefaultWarehouseAndAllocateStock();
    }

    private void createDefaultWarehouseAndAllocateStock() {
        Warehouse defaultWarehouse = warehouseRepository.findByCodeIgnoreCase("DEFAULT")
                .orElseGet(() -> warehouseRepository.save(Warehouse.builder()
                        .code("DEFAULT")
                        .name("기본 창고")
                        .address("미지정")
                        .active(true)
                        .build()));

        for (Product product : productRepository.findAll()) {
            long allocatedAvailable = warehouseStockRepository.sumAvailableQuantityByProductId(product.getId());
            long missing = product.getStockQuantity() - allocatedAvailable;
            if (missing <= 0) continue;
            WarehouseStock stock = warehouseStockRepository.findForUpdate(defaultWarehouse.getId(), product.getId())
                    .orElseGet(() -> WarehouseStock.builder()
                            .warehouse(defaultWarehouse)
                            .product(product)
                            .quantity(0)
                            .build());
            stock.increase(Math.toIntExact(missing));
            warehouseStockRepository.save(stock);
        }
    }

    private void createAdminIfNotExists() {
        if (initialAdminEmail == null || initialAdminEmail.isBlank() || initialAdminPassword == null || initialAdminPassword.isBlank()) {
            log.warn("Initial admin creation skipped because credentials are not configured.");
            return;
        }
        if (!userRepository.existsByEmail(initialAdminEmail)) {
            User admin = User.createAdmin(initialAdminEmail, passwordEncoder.encode(initialAdminPassword), "관리자");
            userRepository.save(admin);
            log.info("Initial admin account created from environment configuration.");
        }
    }

    private void createInitialCategories() {
        if (categoryRepository.count() > 0) return;

        List<String> names = List.of(
                "BEST", "NEW", "원피스", "블라우스", "아우터", "니트", "티셔츠", "스커트", "팬츠", "SALE"
        );
        List<Category> categories = names.stream()
                .map(name -> Category.builder().name(name).build())
                .collect(Collectors.toList());
        categoryRepository.saveAll(categories);
        log.info("Created {} initial categories", categories.size());
    }

    private void createInitialProducts() {
        if (productRepository.count() > 0) return;

        Map<String, Category> catMap = categoryRepository.findAll().stream()
                .collect(Collectors.toMap(Category::getName, c -> c));

        List<Product> products = List.of(
                Product.builder()
                        .category(catMap.get("원피스"))
                        .name("베이직 셔츠 원피스")
                        .description("데일리로 착용하기 좋은 깔끔한 셔츠 원피스입니다.")
                        .price(39000)
                        .stockQuantity(50)
                        .imageUrl("https://placehold.co/400x500?text=Dress")
                        .status(ProductStatus.ON_SALE)
                        .build(),
                Product.builder()
                        .category(catMap.get("블라우스"))
                        .name("오버핏 리넨 블라우스")
                        .description("고급 리넨 소재의 루즈핏 블라우스입니다.")
                        .price(29000)
                        .stockQuantity(30)
                        .imageUrl("https://placehold.co/400x500?text=Blouse")
                        .status(ProductStatus.ON_SALE)
                        .build(),
                Product.builder()
                        .category(catMap.get("아우터"))
                        .name("봄 가을 트렌치 코트")
                        .description("클래식한 디자인의 트렌치 코트로 봄 가을에 활용하기 좋습니다.")
                        .price(89000)
                        .stockQuantity(20)
                        .imageUrl("https://placehold.co/400x500?text=Coat")
                        .status(ProductStatus.ON_SALE)
                        .build(),
                Product.builder()
                        .category(catMap.get("니트"))
                        .name("스트라이프 크롭 니트")
                        .description("스트라이프 패턴의 크롭 길이 니트입니다.")
                        .price(45000)
                        .stockQuantity(40)
                        .imageUrl("https://placehold.co/400x500?text=Knit")
                        .status(ProductStatus.ON_SALE)
                        .build(),
                Product.builder()
                        .category(catMap.get("팬츠"))
                        .name("와이드 데님 팬츠")
                        .description("편안한 핏의 와이드 데님 팬츠입니다.")
                        .price(52000)
                        .stockQuantity(35)
                        .imageUrl("https://placehold.co/400x500?text=Pants")
                        .status(ProductStatus.ON_SALE)
                        .build()
        );
        productRepository.saveAll(products);
        log.info("Created {} initial products", products.size());
    }
}
