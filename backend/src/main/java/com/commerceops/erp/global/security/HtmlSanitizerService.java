package com.commerceops.erp.global.security;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.springframework.stereotype.Service;

@Service
public class HtmlSanitizerService {
    private final PolicyFactory policy = new HtmlPolicyBuilder()
            .allowElements("p", "br", "strong", "b", "em", "i", "ul", "ol", "li", "h1", "h2", "h3", "blockquote", "a", "img")
            .allowAttributes("href").onElements("a")
            .allowUrlProtocols("http", "https", "mailto")
            .requireRelNofollowOnLinks()
            .allowAttributes("src", "alt", "width", "height").onElements("img")
            .allowUrlProtocols("http", "https")
            .toFactory();

    public String sanitize(String html) {
        return html == null ? null : policy.sanitize(html);
    }
}
