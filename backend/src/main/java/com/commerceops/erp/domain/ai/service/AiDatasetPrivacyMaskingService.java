package com.commerceops.erp.domain.ai.service;

import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class AiDatasetPrivacyMaskingService {

    private static final Pattern EMAIL = Pattern.compile("(?i)[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}");
    private static final Pattern PHONE = Pattern.compile("\\b(01[016789])-?\\d{3,4}-?\\d{4}\\b");
    private static final Pattern TOKEN_FIELD = Pattern.compile("(?i)(accessToken|refreshToken|token|password)\\s*[:=]\\s*[^\\s,}]+");
    private static final Pattern RESIDENT_NUMBER = Pattern.compile("\\b\\d{6}-?[1-4]\\d{6}\\b");
    private static final Pattern ROAD_ADDRESS_HINT = Pattern.compile("([가-힣A-Za-z0-9]+(로|길)\\s?\\d{1,5})(\\s?[-\\d]*)?");

    public String maskText(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        String masked = EMAIL.matcher(value).replaceAll("***@***");
        masked = PHONE.matcher(masked).replaceAll("010-****-****");
        masked = RESIDENT_NUMBER.matcher(masked).replaceAll("******-*******");
        masked = TOKEN_FIELD.matcher(masked).replaceAll("$1=***");
        masked = ROAD_ADDRESS_HINT.matcher(masked).replaceAll("[주소 마스킹]");
        return masked;
    }

    public String maskIdentifier(Long id) {
        if (id == null) {
            return "";
        }
        return "U" + Integer.toHexString(("commerceops:" + id).hashCode()).toUpperCase();
    }
}
