package com.commerceops.erp.domain.media.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(MediaStorageProperties.class)
public class MediaWebConfig implements WebMvcConfigurer {

    private final MediaStorageProperties properties;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String pattern = normalizePublicPath(properties.publicPath()) + "/**";
        String location = Path.of(properties.uploadDir()).toAbsolutePath().normalize().toUri().toString();
        registry.addResourceHandler(pattern).addResourceLocations(location);
    }

    private String normalizePublicPath(String publicPath) {
        String normalized = publicPath.startsWith("/") ? publicPath : "/" + publicPath;
        return normalized.endsWith("/") ? normalized.substring(0, normalized.length() - 1) : normalized;
    }
}
