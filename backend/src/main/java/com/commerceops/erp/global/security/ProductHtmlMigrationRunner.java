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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.product-html-migration.mode", havingValue = "scan")
public class ProductHtmlMigrationRunner implements CommandLineRunner {
    private final ProductDetailBlockRepository repository;

    @Override
    @Transactional(readOnly = true)
    public void run(String... args) {
        repository.findAll().stream()
                .filter(block -> block.getBlockType() == ProductDetailBlockType.HTML)
                .map(this::findFinding)
                .filter(finding -> !finding.patterns().isEmpty())
                .forEach(finding -> log.warn("HTML scan finding productId={} productName={} patterns={}",
                        finding.block().getProduct().getId(), finding.block().getProduct().getName(), finding.patterns()));
    }

    private Finding findFinding(ProductDetailBlock block) {
        String content = block.getContent() == null ? "" : block.getContent().toLowerCase(Locale.ROOT);
        List<String> patterns = new ArrayList<>();
        for (String pattern : List.of("<script", "javascript:", "onerror=", "onclick=", "onload=", "<iframe", "<object", "<embed", "data:text/html")) {
            if (content.contains(pattern)) patterns.add(pattern);
        }
        return new Finding(block, patterns);
    }

    private record Finding(ProductDetailBlock block, List<String> patterns) { }
}
