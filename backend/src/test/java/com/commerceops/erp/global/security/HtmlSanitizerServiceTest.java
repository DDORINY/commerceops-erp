package com.commerceops.erp.global.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HtmlSanitizerServiceTest {
    private final HtmlSanitizerService sanitizer = new HtmlSanitizerService();

    @Test
    void removesExecutableMarkupAndKeepsSafeContent() {
        String result = sanitizer.sanitize("<script>alert(1)</script>"
                + "<img src=x onerror=alert(1)>"
                + "<a href=\"javascript:alert(1)\">click</a>"
                + "<iframe src=\"https://evil.example\"></iframe>"
                + "<p>정상 설명</p>");

        assertThat(result).doesNotContainIgnoringCase("script", "onerror", "javascript:", "iframe");
        assertThat(result).contains("정상 설명");
    }

    @Test
    void allowsOnlySafeLinkAndImageAttributes() {
        String result = sanitizer.sanitize("<a href=\"https://example.com\" onclick=\"alert(1)\">safe</a>"
                + "<img src=\"https://example.com/a.png\" alt=\"a\" style=\"display:none\">");

        assertThat(result).contains("href=\"https://example.com\"");
        assertThat(result).contains("src=\"https://example.com/a.png\"");
        assertThat(result).doesNotContainIgnoringCase("onclick", "style");
    }
}
