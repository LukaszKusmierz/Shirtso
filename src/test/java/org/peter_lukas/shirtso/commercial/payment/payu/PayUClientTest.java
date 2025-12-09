package org.peter_lukas.shirtso.commercial.payment.payu;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.peter_lukas.shirtso.commercial.payment.payu.dto.*;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PayUClientTest {

    @Mock
    private RestTemplate restTemplate;

    private PayUProperties properties;
    private PayUClient payUClient;

    @BeforeEach
    void setUp() {
        properties = new PayUProperties(
                "https://secure.snd.payu.com",
                "test-client-id",
                "test-client-secret",
                "300746",
                "test-second-key",
                "https://example.com/api/payu/notify",
                "https://example.com/payment/continue"
        );
        payUClient = new PayUClient(properties);
        // Inject mock RestTemplate
        ReflectionTestUtils.setField(payUClient, "restTemplate", restTemplate);
    }

    @Test
    void getAccessToken_FirstCall_RequestsNewToken() {
        // given
        PayUOAuthResponseDto oauthResponse = new PayUOAuthResponseDto(
                "test-access-token",
                "bearer",
                3600,
                "client_credentials"
        );

        when(restTemplate.exchange(
                eq("https://secure.snd.payu.com/pl/standard/user/oauth/authorize"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(PayUOAuthResponseDto.class)
        )).thenReturn(ResponseEntity.ok(oauthResponse));

        // when
        String token = payUClient.getAccessToken();

        // then
        assertThat(token).isEqualTo("test-access-token");
        verify(restTemplate).exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(PayUOAuthResponseDto.class)
        );
    }

    @Test
    void getAccessToken_WithCachedToken_ReturnsCachedToken() {
        // given
        ReflectionTestUtils.setField(payUClient, "cachedAccessToken", "cached-token");
        ReflectionTestUtils.setField(payUClient, "tokenExpiresAt", Instant.now().plusSeconds(3600));

        // when
        String token = payUClient.getAccessToken();

        // then
        assertThat(token).isEqualTo("cached-token");
        verifyNoInteractions(restTemplate);
    }

    @Test
    void getAccessToken_WithExpiredToken_RequestsNewToken() {
        // given
        ReflectionTestUtils.setField(payUClient, "cachedAccessToken", "expired-token");
        ReflectionTestUtils.setField(payUClient, "tokenExpiresAt", Instant.now().minusSeconds(100));

        PayUOAuthResponseDto oauthResponse = new PayUOAuthResponseDto(
                "new-access-token",
                "bearer",
                3600,
                "client_credentials"
        );

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(PayUOAuthResponseDto.class)
        )).thenReturn(ResponseEntity.ok(oauthResponse));

        // when
        String token = payUClient.getAccessToken();

        // then
        assertThat(token).isEqualTo("new-access-token");
        verify(restTemplate).exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(PayUOAuthResponseDto.class)
        );
    }

    @Test
    void getAccessToken_WhenOAuthFails_ThrowsPayUApiException() {
        // given
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(PayUOAuthResponseDto.class)
        )).thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        // when & then
        assertThatThrownBy(() -> payUClient.getAccessToken())
                .isInstanceOf(PayUApiException.class)
                .hasMessageContaining("Failed to authenticate with PayU");
    }

    @Test
    void getAccessToken_WhenEmptyResponse_ThrowsPayUApiException() {
        // given
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(PayUOAuthResponseDto.class)
        )).thenReturn(ResponseEntity.ok(null));

        // when & then
        assertThatThrownBy(() -> payUClient.getAccessToken())
                .isInstanceOf(PayUApiException.class)
                .hasMessageContaining("Empty OAuth response");
    }

    @Test
    void getAccessToken_SendsCorrectCredentials() {
        // given
        PayUOAuthResponseDto oauthResponse = new PayUOAuthResponseDto(
                "test-token",
                "bearer",
                3600,
                "client_credentials"
        );

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(PayUOAuthResponseDto.class)
        )).thenReturn(ResponseEntity.ok(oauthResponse));

        // when
        payUClient.getAccessToken();

        // then
        @SuppressWarnings("unchecked")
        ArgumentCaptor<HttpEntity<MultiValueMap<String, String>>> captor =
                ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(
                anyString(),
                eq(HttpMethod.POST),
                captor.capture(),
                eq(PayUOAuthResponseDto.class)
        );

        HttpEntity<MultiValueMap<String, String>> request = captor.getValue();
        assertThat(request.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = request.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getFirst("grant_type")).isEqualTo("client_credentials");
        assertThat(body.getFirst("client_id")).isEqualTo("test-client-id");
        assertThat(body.getFirst("client_secret")).isEqualTo("test-client-secret");
    }

    @Test
    void createOrder_Success_ReturnsOrderResponse() {
        // given
        // First mock OAuth
        ReflectionTestUtils.setField(payUClient, "cachedAccessToken", "test-token");
        ReflectionTestUtils.setField(payUClient, "tokenExpiresAt", Instant.now().plusSeconds(3600));

        PayUOrderRequestDto orderRequest = new PayUOrderRequestDto(
                "https://notify.url",
                "https://continue.url",
                "127.0.0.1",
                "300746",
                "Test order",
                "PLN",
                "10000",
                "EXT-123",
                new PayUOrderRequestDto.PayUBuyer("test@example.com", null, "John", "Doe", "en"),
                List.of(new PayUOrderRequestDto.PayUProduct("Product", "10000", "1")),
                null
        );

        PayUOrderResponseDto.PayUStatus status = new PayUOrderResponseDto.PayUStatus("SUCCESS", null);
        PayUOrderResponseDto expectedResponse = new PayUOrderResponseDto(
                status,
                "https://redirect.url",
                "ORDER-123",
                "EXT-123"
        );

        when(restTemplate.exchange(
                eq("https://secure.snd.payu.com/api/v2_1/orders"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(PayUOrderResponseDto.class)
        )).thenReturn(ResponseEntity.ok(expectedResponse));

        // when
        PayUOrderResponseDto result = payUClient.createOrder(orderRequest);

        // then
        assertThat(result.orderId()).isEqualTo("ORDER-123");
        assertThat(result.status().statusCode()).isEqualTo("SUCCESS");
    }

    @Test
    void createOrder_IncludesBearerToken() {
        // given
        ReflectionTestUtils.setField(payUClient, "cachedAccessToken", "bearer-test-token");
        ReflectionTestUtils.setField(payUClient, "tokenExpiresAt", Instant.now().plusSeconds(3600));

        PayUOrderRequestDto orderRequest = new PayUOrderRequestDto(
                "https://notify.url", "https://continue.url", "127.0.0.1",
                "300746", "Test", "PLN", "1000", "EXT-123",
                null, List.of(), null
        );

        PayUOrderResponseDto.PayUStatus status = new PayUOrderResponseDto.PayUStatus("SUCCESS", null);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(PayUOrderResponseDto.class)
        )).thenReturn(ResponseEntity.ok(new PayUOrderResponseDto(status, null, "ORDER-123", "EXT-123")));

        // when
        payUClient.createOrder(orderRequest);

        // then
        @SuppressWarnings("unchecked")
        ArgumentCaptor<HttpEntity<PayUOrderRequestDto>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(
                anyString(),
                eq(HttpMethod.POST),
                captor.capture(),
                eq(PayUOrderResponseDto.class)
        );

        HttpHeaders headers = captor.getValue().getHeaders();
        assertThat(headers.getFirst(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer bearer-test-token");
        assertThat(headers.getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
    }

    @Test
    void createOrder_WhenApiFails_ThrowsPayUApiException() {
        // given
        ReflectionTestUtils.setField(payUClient, "cachedAccessToken", "test-token");
        ReflectionTestUtils.setField(payUClient, "tokenExpiresAt", Instant.now().plusSeconds(3600));

        PayUOrderRequestDto orderRequest = new PayUOrderRequestDto(
                "https://notify.url", "https://continue.url", "127.0.0.1",
                "300746", "Test", "PLN", "1000", "EXT-123",
                null, List.of(), null
        );

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(PayUOrderResponseDto.class)
        )).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Invalid request"));

        // when & then
        assertThatThrownBy(() -> payUClient.createOrder(orderRequest))
                .isInstanceOf(PayUApiException.class)
                .hasMessageContaining("Failed to create PayU order");
    }

    @Test
    void createOrder_WhenEmptyResponse_ThrowsPayUApiException() {
        // given
        ReflectionTestUtils.setField(payUClient, "cachedAccessToken", "test-token");
        ReflectionTestUtils.setField(payUClient, "tokenExpiresAt", Instant.now().plusSeconds(3600));

        PayUOrderRequestDto orderRequest = new PayUOrderRequestDto(
                "https://notify.url", "https://continue.url", "127.0.0.1",
                "300746", "Test", "PLN", "1000", "EXT-123",
                null, List.of(), null
        );

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(PayUOrderResponseDto.class)
        )).thenReturn(ResponseEntity.ok(null));

        // when & then
        assertThatThrownBy(() -> payUClient.createOrder(orderRequest))
                .isInstanceOf(PayUApiException.class)
                .hasMessageContaining("Empty order response");
    }

    @Test
    void createRefund_Success_ReturnsRefundResponse() {
        // given
        ReflectionTestUtils.setField(payUClient, "cachedAccessToken", "test-token");
        ReflectionTestUtils.setField(payUClient, "tokenExpiresAt", Instant.now().plusSeconds(3600));

        String orderId = "ORDER-123";
        PayURefundRequestDto refundRequest = new PayURefundRequestDto(
                new PayURefundRequestDto.PayURefund("Refund reason", "5000", "EXT-REFUND-123")
        );

        PayURefundResponseDto.PayUStatus status = new PayURefundResponseDto.PayUStatus("SUCCESS", null);
        PayURefundResponseDto.PayURefund refundDetails =
                new PayURefundResponseDto.PayURefund("REFUND-123", null,"5000", "PLN",null, null, "FINALIZED", null);
        PayURefundResponseDto expectedResponse = new PayURefundResponseDto(status, orderId, refundDetails);

        when(restTemplate.exchange(
                eq("https://secure.snd.payu.com/api/v2_1/orders/ORDER-123/refunds"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(PayURefundResponseDto.class)
        )).thenReturn(ResponseEntity.ok(expectedResponse));

        // when
        PayURefundResponseDto result = payUClient.createRefund(orderId, refundRequest);

        // then
        assertThat(result.status().statusCode()).isEqualTo("SUCCESS");
        assertThat(result.refund().refundId()).isEqualTo("REFUND-123");
    }

    @Test
    void createRefund_WhenApiFails_ThrowsPayUApiException() {
        // given
        ReflectionTestUtils.setField(payUClient, "cachedAccessToken", "test-token");
        ReflectionTestUtils.setField(payUClient, "tokenExpiresAt", Instant.now().plusSeconds(3600));

        String orderId = "ORDER-123";
        PayURefundRequestDto refundRequest = new PayURefundRequestDto(
                new PayURefundRequestDto.PayURefund("Refund reason", "5000", "EXT-REFUND-123")
        );

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(PayURefundResponseDto.class)
        )).thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND, "Order not found"));

        // when & then
        assertThatThrownBy(() -> payUClient.createRefund(orderId, refundRequest))
                .isInstanceOf(PayUApiException.class)
                .hasMessageContaining("Failed to create PayU refund");
    }
}
