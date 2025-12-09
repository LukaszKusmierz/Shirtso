package org.peter_lukas.shirtso.commercial.payment.payu;

import lombok.extern.slf4j.Slf4j;
import org.peter_lukas.shirtso.commercial.payment.PaymentDetails;
import org.peter_lukas.shirtso.commercial.payment.PaymentGateway;
import org.peter_lukas.shirtso.commercial.payment.PaymentMethod;
import org.peter_lukas.shirtso.commercial.payment.payu.dto.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@Primary
@ConditionalOnProperty(name = "app.payu.enabled", havingValue = "true")
public class PayUPaymentGateway implements PaymentGateway {

    private static final String DEFAULT_CURRENCY = "PLN";
    private static final String DEFAULT_DESCRIPTION = "Shirtso Order Payment";

    private final PayUClient payUClient;
    private final PayUProperties properties;

    public PayUPaymentGateway(PayUClient payUClient, PayUProperties properties) {
        this.payUClient = payUClient;
        this.properties = properties;
        log.info("PayU Payment Gateway initialized with POS ID: {}", properties.posId());
    }

    @Override
    public String processPayment(PaymentMethod method, BigDecimal amount, PaymentDetails details) {
        log.info("Processing PayU payment: method={}, amount={}", method, amount);

        String extOrderId = generateExtOrderId();
        String amountInMinorUnits = convertToMinorUnits(amount);

        PayUOrderRequestDto.PayUPayMethod payMethod = createPayMethod(method, details);

        PayUOrderRequestDto orderRequest = new PayUOrderRequestDto(
                properties.notifyUrl(),
                properties.continueUrl(),
                "127.0.0.1",
                properties.posId(),
                DEFAULT_DESCRIPTION,
                DEFAULT_CURRENCY,
                amountInMinorUnits,
                extOrderId,
                createBuyer(details),
                createProducts(amount),
                payMethod != null ? new PayUOrderRequestDto.PayUPayMethods(payMethod) : null
        );

        try {
            PayUOrderResponseDto response = payUClient.createOrder(orderRequest);

            if (!"SUCCESS".equals(response.status().statusCode()) &&
                    !"WARNING_CONTINUE_3DS".equals(response.status().statusCode()) &&
                    !"WARNING_CONTINUE_REDIRECT".equals(response.status().statusCode())) {
                log.error("PayU payment failed with status: {}", response.status().statusCode());
                throw new PayUApiException("Payment failed: " + response.status().statusDesc());
            }

            String transactionId = response.orderId();
            log.info("PayU payment successful: orderId={}, extOrderId={}", transactionId, extOrderId);

            return transactionId;

        } catch (PayUApiException e) {
            log.error("PayU payment processing failed: {}", e.getMessage());
            throw new RuntimeException("Payment processing failed: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean refundPayment(String transactionId, BigDecimal amount) {
        log.info("Processing PayU refund: orderId={}, amount={}", transactionId, amount);

        String amountInMinorUnits = convertToMinorUnits(amount);
        String extRefundId = generateExtRefundId();

        PayURefundRequestDto refundRequest = new PayURefundRequestDto(
                new PayURefundRequestDto.PayURefund(
                        "Order refund",
                        amountInMinorUnits,
                        extRefundId
                )
        );

        try {
            PayURefundResponseDto response = payUClient.createRefund(transactionId, refundRequest);

            boolean success = "SUCCESS".equals(response.status().statusCode());
            log.info("PayU refund {}: refundId={}", success ? "successful" : "failed",
                    response.refund() != null ? response.refund().refundId() : "N/A");

            return success;

        } catch (PayUApiException e) {
            log.error("PayU refund processing failed: {}", e.getMessage());
            return false;
        }
    }

    private PayUOrderRequestDto.PayUPayMethod createPayMethod(PaymentMethod method, PaymentDetails details) {
        if (method == PaymentMethod.CREDIT_CARD && details.getCardNumber() != null) {
            String expiryDate = details.getExpiryDate();
            String expiryMonth = "12";
            String expiryYear = "29";

            if (expiryDate != null && expiryDate.contains("/")) {
                String[] parts = expiryDate.split("/");
                expiryMonth = parts[0];
                expiryYear = parts.length > 1 ? parts[1] : "29";
                if (expiryYear.length() == 2) {
                    expiryYear = "20" + expiryYear;
                }
            }

            return new PayUOrderRequestDto.PayUPayMethod(
                    "CARD_TOKEN",
                    null,
                    new PayUOrderRequestDto.PayUCardData(
                            details.getCardNumber(),
                            expiryMonth,
                            expiryYear,
                            details.getCvv()
                    )
            );
        }

        return null;
    }

    private PayUOrderRequestDto.PayUBuyer createBuyer(PaymentDetails details) {
        String name = details.getCardHolderName();
        String firstName = "Customer";
        String lastName = "";

        if (name != null && !name.isBlank()) {
            String[] nameParts = name.trim().split("\\s+", 2);
            firstName = nameParts[0];
            lastName = nameParts.length > 1 ? nameParts[1] : "";
        }

        return new PayUOrderRequestDto.PayUBuyer(
                "customer@example.com",
                null,
                firstName,
                lastName,
                "en"
        );
    }

    private List<PayUOrderRequestDto.PayUProduct> createProducts(BigDecimal amount) {
        return List.of(
                new PayUOrderRequestDto.PayUProduct(
                        "Order Payment",
                        convertToMinorUnits(amount),
                        "1"
                )
        );
    }

    private String convertToMinorUnits(BigDecimal amount) {
        return amount.multiply(BigDecimal.valueOf(100)).toBigInteger().toString();
    }

    private String generateExtOrderId() {
        return "SHIRTSO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String generateExtRefundId() {
        return "REFUND-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
