package org.peter_lukas.shirtso.commercial.payment.payu;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.peter_lukas.shirtso.commercial.payment.PaymentDetails;
import org.peter_lukas.shirtso.commercial.payment.PaymentGateway;
import org.peter_lukas.shirtso.commercial.payment.PaymentMethod;
import org.peter_lukas.shirtso.commercial.payment.payu.dto.*;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PayUPaymentGatewayTest {

    @Mock
    private PayUClient payUClient;

    private PayUProperties properties;
    private PayUPaymentGateway paymentGateway;

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
        paymentGateway = new PayUPaymentGateway(payUClient, properties);
    }

    @Test
    void processPayment_WithCreditCard_CreatesOrderSuccessfully() {
        // given
        BigDecimal amount = new BigDecimal("99.99");
        PaymentDetails details = PaymentDetails.builder()
                .cardNumber("4111111111111111")
                .cardHolderName("John Doe")
                .expiryDate("12/25")
                .cvv("123")
                .build();

        PayUOrderResponseDto.PayUStatus status = new PayUOrderResponseDto.PayUStatus("SUCCESS", null);
        PayUOrderResponseDto response = new PayUOrderResponseDto(
                status,
                "https://redirect.url",
                "PAYU-ORDER-123",
                "SHIRTSO-ABC12345"
        );

        when(payUClient.createOrder(any(PayUOrderRequestDto.class))).thenReturn(response);

        // when
        PaymentGateway.PaymentResult transactionId = paymentGateway.processPayment(PaymentMethod.CREDIT_CARD, amount, details);

        // then
        assertThat(transactionId.transactionId()).isEqualTo("PAYU-ORDER-123");

        ArgumentCaptor<PayUOrderRequestDto> captor = ArgumentCaptor.forClass(PayUOrderRequestDto.class);
        verify(payUClient).createOrder(captor.capture());

        PayUOrderRequestDto capturedRequest = captor.getValue();
        assertThat(capturedRequest.totalAmount()).isEqualTo("9999"); // Amount in minor units (grosze)
        assertThat(capturedRequest.currencyCode()).isEqualTo("PLN");
        assertThat(capturedRequest.merchantPosId()).isEqualTo("300746");
        assertThat(capturedRequest.notifyUrl()).isEqualTo("https://example.com/api/payu/notify");
        assertThat(capturedRequest.continueUrl()).isEqualTo("https://example.com/payment/continue");
        assertThat(capturedRequest.extOrderId()).startsWith("SHIRTSO-");
    }

    @Test
    void processPayment_WithCardData_IncludesPayMethodInRequest() {
        // given
        BigDecimal amount = new BigDecimal("50.00");
        PaymentDetails details = PaymentDetails.builder()
                .cardNumber("4111111111111111")
                .cardHolderName("Jane Smith")
                .expiryDate("06/28")
                .cvv("456")
                .build();

        PayUOrderResponseDto.PayUStatus status = new PayUOrderResponseDto.PayUStatus("SUCCESS", null);
        PayUOrderResponseDto response = new PayUOrderResponseDto(status, null, "ORDER-456", "EXT-456");

        when(payUClient.createOrder(any(PayUOrderRequestDto.class))).thenReturn(response);

        // when
        paymentGateway.processPayment(PaymentMethod.CREDIT_CARD, amount, details);

        // then
        ArgumentCaptor<PayUOrderRequestDto> captor = ArgumentCaptor.forClass(PayUOrderRequestDto.class);
        verify(payUClient).createOrder(captor.capture());

        PayUOrderRequestDto capturedRequest = captor.getValue();
        assertThat(capturedRequest.payMethods()).isNotNull();
        assertThat(capturedRequest.payMethods().payMethod().type()).isEqualTo("CARD_TOKEN");
        assertThat(capturedRequest.payMethods().payMethod().card().number()).isEqualTo("4111111111111111");
        assertThat(capturedRequest.payMethods().payMethod().card().expirationMonth()).isEqualTo("06");
        assertThat(capturedRequest.payMethods().payMethod().card().expirationYear()).isEqualTo("2028");
        assertThat(capturedRequest.payMethods().payMethod().card().cvv()).isEqualTo("456");
    }

    @Test
    void processPayment_WithoutCardDetails_DoesNotIncludePayMethod() {
        // given
        BigDecimal amount = new BigDecimal("25.00");
        PaymentDetails details = PaymentDetails.builder()
                .cardHolderName("PayPal User")
                .build();

        PayUOrderResponseDto.PayUStatus status = new PayUOrderResponseDto.PayUStatus("SUCCESS", null);
        PayUOrderResponseDto response = new PayUOrderResponseDto(status, null, "ORDER-789", "EXT-789");

        when(payUClient.createOrder(any(PayUOrderRequestDto.class))).thenReturn(response);

        // when
        paymentGateway.processPayment(PaymentMethod.PAYPAL, amount, details);

        // then
        ArgumentCaptor<PayUOrderRequestDto> captor = ArgumentCaptor.forClass(PayUOrderRequestDto.class);
        verify(payUClient).createOrder(captor.capture());

        PayUOrderRequestDto capturedRequest = captor.getValue();
        assertThat(capturedRequest.payMethods()).isNull();
    }

    @Test
    void processPayment_With3DSRequired_ReturnsTransactionId() {
        // given
        BigDecimal amount = new BigDecimal("100.00");
        PaymentDetails details = PaymentDetails.builder()
                .cardNumber("4111111111111111")
                .cardHolderName("3DS User")
                .expiryDate("12/25")
                .cvv("123")
                .build();

        PayUOrderResponseDto.PayUStatus status = new PayUOrderResponseDto.PayUStatus("WARNING_CONTINUE_3DS", null);
        PayUOrderResponseDto response = new PayUOrderResponseDto(
                status,
                "https://3ds.redirect.url",
                "3DS-ORDER-123",
                "EXT-3DS"
        );

        when(payUClient.createOrder(any(PayUOrderRequestDto.class))).thenReturn(response);

        // when
        PaymentGateway.PaymentResult transactionId = paymentGateway.processPayment(PaymentMethod.CREDIT_CARD, amount, details);

        // then
        assertThat(transactionId.transactionId()).isEqualTo("3DS-ORDER-123");
    }

    @Test
    void processPayment_WithRedirectRequired_ReturnsTransactionId() {
        // given
        BigDecimal amount = new BigDecimal("75.50");
        PaymentDetails details = PaymentDetails.builder().build();

        PayUOrderResponseDto.PayUStatus status = new PayUOrderResponseDto.PayUStatus("WARNING_CONTINUE_REDIRECT", null);
        PayUOrderResponseDto response = new PayUOrderResponseDto(
                status,
                "https://payment.redirect.url",
                "REDIRECT-ORDER-123",
                "EXT-REDIRECT"
        );

        when(payUClient.createOrder(any(PayUOrderRequestDto.class))).thenReturn(response);

        // when
        PaymentGateway.PaymentResult transactionId = paymentGateway.processPayment(PaymentMethod.BANK_TRANSFER, amount, details);

        // then
        assertThat(transactionId.transactionId()).isEqualTo("REDIRECT-ORDER-123");
    }

    @Test
    void processPayment_WhenPayUFails_ThrowsException() {
        // given
        BigDecimal amount = new BigDecimal("50.00");
        PaymentDetails details = PaymentDetails.builder().build();

        PayUOrderResponseDto.PayUStatus status = new PayUOrderResponseDto.PayUStatus("ERROR", "Insufficient funds");
        PayUOrderResponseDto response = new PayUOrderResponseDto(status, null, null, "EXT-FAIL");

        when(payUClient.createOrder(any(PayUOrderRequestDto.class))).thenReturn(response);

        // when & then
        assertThatThrownBy(() -> paymentGateway.processPayment(PaymentMethod.CREDIT_CARD, amount, details))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Payment processing failed");
    }

    @Test
    void processPayment_WhenPayUClientThrowsException_PropagatesException() {
        // given
        BigDecimal amount = new BigDecimal("50.00");
        PaymentDetails details = PaymentDetails.builder().build();

        when(payUClient.createOrder(any(PayUOrderRequestDto.class)))
                .thenThrow(new PayUApiException("Connection timeout"));

        // when & then
        assertThatThrownBy(() -> paymentGateway.processPayment(PaymentMethod.CREDIT_CARD, amount, details))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Payment processing failed")
                .hasMessageContaining("Connection timeout");
    }

    @Test
    void processPayment_ConvertsAmountToMinorUnitsCorrectly() {
        // given
        BigDecimal amount = new BigDecimal("123.45");
        PaymentDetails details = PaymentDetails.builder().build();

        PayUOrderResponseDto.PayUStatus status = new PayUOrderResponseDto.PayUStatus("SUCCESS", null);
        PayUOrderResponseDto response = new PayUOrderResponseDto(status, null, "ORDER-123", "EXT-123");

        when(payUClient.createOrder(any(PayUOrderRequestDto.class))).thenReturn(response);

        // when
        paymentGateway.processPayment(PaymentMethod.CREDIT_CARD, amount, details);

        // then
        ArgumentCaptor<PayUOrderRequestDto> captor = ArgumentCaptor.forClass(PayUOrderRequestDto.class);
        verify(payUClient).createOrder(captor.capture());

        assertThat(captor.getValue().totalAmount()).isEqualTo("12345");
    }

    @Test
    void processPayment_ParsesCardHolderNameCorrectly() {
        // given
        BigDecimal amount = new BigDecimal("10.00");
        PaymentDetails details = PaymentDetails.builder()
                .cardHolderName("John Michael Doe")
                .build();

        PayUOrderResponseDto.PayUStatus status = new PayUOrderResponseDto.PayUStatus("SUCCESS", null);
        PayUOrderResponseDto response = new PayUOrderResponseDto(status, null, "ORDER-123", "EXT-123");

        when(payUClient.createOrder(any(PayUOrderRequestDto.class))).thenReturn(response);

        // when
        paymentGateway.processPayment(PaymentMethod.CREDIT_CARD, amount, details);

        // then
        ArgumentCaptor<PayUOrderRequestDto> captor = ArgumentCaptor.forClass(PayUOrderRequestDto.class);
        verify(payUClient).createOrder(captor.capture());

        assertThat(captor.getValue().buyer().firstName()).isEqualTo("John");
        assertThat(captor.getValue().buyer().lastName()).isEqualTo("Michael Doe");
    }

    @Test
    void refundPayment_Success_ReturnsTrue() {
        // given
        String transactionId = "PAYU-ORDER-123";
        BigDecimal amount = new BigDecimal("50.00");

        PayURefundResponseDto.PayUStatus status = new PayURefundResponseDto.PayUStatus("SUCCESS", null);
        PayURefundResponseDto.PayURefund refundDetails =
                new PayURefundResponseDto.PayURefund("REFUND-123", null,"5000", "PLN",null, null, "FINALIZED", null);
        PayURefundResponseDto response = new PayURefundResponseDto(status, "PAYU-ORDER-123", refundDetails);

        when(payUClient.createRefund(eq(transactionId), any(PayURefundRequestDto.class))).thenReturn(response);

        // when
        boolean result = paymentGateway.refundPayment(transactionId, amount);

        // then
        assertThat(result).isTrue();

        ArgumentCaptor<PayURefundRequestDto> captor = ArgumentCaptor.forClass(PayURefundRequestDto.class);
        verify(payUClient).createRefund(eq(transactionId), captor.capture());

        assertThat(captor.getValue().refund().amount()).isEqualTo("5000");
        assertThat(captor.getValue().refund().extRefundId()).startsWith("REFUND-");
    }

    @Test
    void refundPayment_WhenFails_ReturnsFalse() {
        // given
        String transactionId = "PAYU-ORDER-123";
        BigDecimal amount = new BigDecimal("50.00");

        PayURefundResponseDto.PayUStatus status = new PayURefundResponseDto.PayUStatus("ERROR", "Refund failed");
        PayURefundResponseDto response = new PayURefundResponseDto(status, "PAYU-ORDER-123", null);

        when(payUClient.createRefund(eq(transactionId), any(PayURefundRequestDto.class))).thenReturn(response);

        // when
        boolean result = paymentGateway.refundPayment(transactionId, amount);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void refundPayment_WhenExceptionThrown_ReturnsFalse() {
        // given
        String transactionId = "PAYU-ORDER-123";
        BigDecimal amount = new BigDecimal("50.00");

        when(payUClient.createRefund(eq(transactionId), any(PayURefundRequestDto.class)))
                .thenThrow(new PayUApiException("Network error"));

        // when
        boolean result = paymentGateway.refundPayment(transactionId, amount);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void processPayment_WithTwoDigitExpiryYear_ConvertsToFourDigit() {
        // given
        BigDecimal amount = new BigDecimal("10.00");
        PaymentDetails details = PaymentDetails.builder()
                .cardNumber("4111111111111111")
                .expiryDate("03/26")
                .cvv("123")
                .build();

        PayUOrderResponseDto.PayUStatus status = new PayUOrderResponseDto.PayUStatus("SUCCESS", null);
        PayUOrderResponseDto response = new PayUOrderResponseDto(status, null, "ORDER-123", "EXT-123");

        when(payUClient.createOrder(any(PayUOrderRequestDto.class))).thenReturn(response);

        // when
        paymentGateway.processPayment(PaymentMethod.CREDIT_CARD, amount, details);

        // then
        ArgumentCaptor<PayUOrderRequestDto> captor = ArgumentCaptor.forClass(PayUOrderRequestDto.class);
        verify(payUClient).createOrder(captor.capture());

        assertThat(captor.getValue().payMethods().payMethod().card().expirationYear()).isEqualTo("2026");
    }
}
