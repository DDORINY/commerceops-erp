package com.commerceops.erp.domain.payment.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.Map;

@Component
public class RestTossPaymentClient implements TossPaymentClient {
    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String secretKey;

    public RestTossPaymentClient(RestClient.Builder builder, ObjectMapper objectMapper,
                                 @Value("${commerceops.toss.api-url}") String apiUrl,
                                 @Value("${commerceops.toss.secret-key:}") String secretKey) {
        this.restClient = builder.baseUrl(apiUrl).build();
        this.objectMapper = objectMapper;
        this.secretKey = secretKey;
    }

    @Override
    public TossConfirmResult confirm(String paymentKey, String orderId, Integer amount, String idempotencyKey) {
        if (secretKey == null || secretKey.isBlank()) {
            throw new TossPaymentClientException("TOSS_SECRET_KEY_MISSING", "토스페이먼츠 문서용 테스트 시크릿 키가 설정되지 않았습니다.", 503, false);
        }
        if (!secretKey.startsWith("test_gsk_")) {
            throw new TossPaymentClientException("TOSS_TEST_SECRET_KEY_REQUIRED",
                    "라이브 키는 사용할 수 없습니다. 문서용 test_gsk 키를 설정해주세요.", 503, false);
        }
        String authorization = "Basic " + Base64.getEncoder().encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));
        try {
            String raw = restClient.post().uri("/v1/payments/confirm")
                    .header(HttpHeaders.AUTHORIZATION, authorization)
                    .header("Idempotency-Key", idempotencyKey)
                    .body(Map.of("paymentKey", paymentKey, "orderId", orderId, "amount", amount))
                    .retrieve().body(String.class);
            JsonNode json = objectMapper.readTree(raw);
            String approvedAt = json.path("approvedAt").asText(null);
            return new TossConfirmResult(json.path("status").asText(), json.path("paymentKey").asText(),
                    json.path("orderId").asText(), json.path("totalAmount").asInt(), json.path("method").asText(),
                    approvedAt == null ? null : OffsetDateTime.parse(approvedAt).toLocalDateTime(), raw);
        } catch (RestClientResponseException e) {
            String code = "TOSS_APPROVAL_FAILED";
            String message = "토스페이먼츠 승인이 거절되었습니다.";
            try {
                JsonNode error = objectMapper.readTree(e.getResponseBodyAsString());
                code = error.path("code").asText(code);
                message = error.path("message").asText(message);
            } catch (Exception ignored) { }
            throw new TossPaymentClientException(code, message, e.getStatusCode().value(), e.getStatusCode().is5xxServerError());
        } catch (RestClientException e) {
            throw new TossPaymentClientException("TOSS_NETWORK_ERROR", "토스페이먼츠에 연결할 수 없습니다. 잠시 후 다시 시도해주세요.", 503, true);
        } catch (Exception e) {
            throw new TossPaymentClientException("INVALID_TOSS_RESPONSE", "토스페이먼츠 응답을 확인할 수 없습니다.", 502, true);
        }
    }
}
