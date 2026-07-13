package com.commerceops.erp.global.security;

import com.commerceops.erp.domain.product.entity.ProductDetailBlock;
import com.commerceops.erp.domain.product.enums.ProductDetailBlockType;
import com.commerceops.erp.domain.product.repository.ProductDetailBlockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.product-html-migration.mode", havingValue = "sanitize")
public class ProductHtmlSanitizeRunner implements CommandLineRunner {
    private final ProductDetailBlockRepository repository;
    private final HtmlSanitizerService sanitizer;

    @Override
    @Transactional
    public void run(String... args) {
        int changed = 0;
        for (ProductDetailBlock block : repository.findAll()) {
            if (block.getBlockType() != ProductDetailBlockType.HTML || block.getContent() == null) continue;
            String sanitized = sanitizer.sanitize(block.getContent());
            if (!sanitized.equals(block.getContent())) {
                block.replaceContent(sanitized);
                changed++;
                log.info("Sanitized product HTML productId={}", block.getProduct().getId());
            }
        }
        log.info("Product HTML sanitization completed changedCount={}", changed);
    }
}
