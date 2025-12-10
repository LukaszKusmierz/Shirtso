package org.peter_lukas.shirtso.commercial.payment.payu;

import lombok.extern.slf4j.Slf4j;
import org.peter_lukas.shirtso.commercial.payment.payu.dto.*;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Component
public class PayUClient {

    private final PayUProperties properties;
    private final RestTemplate restTemplate;
    private final ReentrantLock tokenLock = new ReentrantLock();

    private String cachedAccessToken;
    private Instant tokenExpiresAt;

    public PayUClient(PayUProperties properties) {
        this.properties = properties;
        this.restTemplate = createRestTemplate();
    }

    private RestTemplate createRestTemplate() {
        RestTemplate rt = new RestTemplate();
        rt.getInterceptors().add((request, body, execution) -> {
            log.debug("PayU Request: {} {}", request.getMethod(), request.getURI());
            return execution.execute(request, body);
        });
        return rt;
    }

    public String getAccessToken() {
        tokenLock.lock();
        try {
            if (cachedAccessToken != null && tokenExpiresAt != null && Instant.now().isBefore(tokenExpiresAt)) {
                log.debug("Using cached PayU access token");
                return cachedAccessToken;
            }

            log.info("Requesting new PayU OAuth token");

            String url = properties.baseUrl() + "/pl/standard/user/oauth/token";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "client_credentials");
            body.add("client_id", properties.clientId());
            body.add("client_secret", properties.clientSecret());

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

            ResponseEntity<PayUOAuthResponseDto> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    PayUOAuthResponseDto.class
            );

            if (response.getBody() == null) {
                throw new PayUApiException("Empty OAuth response from PayU");
            }

            PayUOAuthResponseDto oauthResponse = response.getBody();
            cachedAccessToken = oauthResponse.accessToken();
            tokenExpiresAt = Instant.now().plusSeconds(oauthResponse.expiresIn() - 60);

            log.info("PayU OAuth token obtained successfully, expires in {} seconds", oauthResponse.expiresIn());
            return cachedAccessToken;

        } catch (HttpClientErrorException e) {
            log.error("Failed to get PayU OAuth token: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new PayUApiException("Failed to authenticate with PayU: " + e.getMessage(), e);
        } finally {
            tokenLock.unlock();
        }
    }

    public PayUOrderResponseDto createOrder(PayUOrderRequestDto orderRequest) {
        log.info("Creating PayU order for extOrderId: {}", orderRequest.extOrderId());

        String url = properties.baseUrl() + "/api/v2_1/orders";

        HttpHeaders headers = createAuthHeaders();

        HttpEntity<PayUOrderRequestDto> request = new HttpEntity<>(orderRequest, headers);

        try {
            ResponseEntity<PayUOrderResponseDto> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    PayUOrderResponseDto.class
            );

            PayUOrderResponseDto orderResponse = response.getBody();
            if (orderResponse == null) {
                throw new PayUApiException("Empty order response from PayU");
            }

            log.info("PayU order created successfully: orderId={}, status={}",
                    orderResponse.orderId(), orderResponse.status().statusCode());

            return orderResponse;

        } catch (HttpClientErrorException e) {
            log.error("Failed to create PayU order: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new PayUApiException("Failed to create PayU order: " + e.getMessage(), e);
        }
    }

    public PayURefundResponseDto createRefund(String orderId, PayURefundRequestDto refundRequest) {
        log.info("Creating PayU refund for orderId: {}", orderId);

        String url = properties.baseUrl() + "/api/v2_1/orders/" + orderId + "/refunds";

        HttpHeaders headers = createAuthHeaders();

        HttpEntity<PayURefundRequestDto> request = new HttpEntity<>(refundRequest, headers);

        try {
            ResponseEntity<PayURefundResponseDto> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    PayURefundResponseDto.class
            );

            PayURefundResponseDto refundResponse = response.getBody();
            if (refundResponse == null) {
                throw new PayUApiException("Empty refund response from PayU");
            }

            log.info("PayU refund created successfully: refundId={}, status={}",
                    refundResponse.refund().refundId(), refundResponse.status().statusCode());

            return refundResponse;

        } catch (HttpClientErrorException e) {
            log.error("Failed to create PayU refund: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new PayUApiException("Failed to create PayU refund: " + e.getMessage(), e);
        }
    }

    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(getAccessToken());
        return headers;
    }
}
