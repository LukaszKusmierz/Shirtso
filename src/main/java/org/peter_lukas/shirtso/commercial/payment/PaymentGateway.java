package org.peter_lukas.shirtso.commercial.payment;

import java.math.BigDecimal;

public interface PaymentGateway {

    PaymentResult processPayment(PaymentMethod method, BigDecimal amount, PaymentDetails details, Integer orderId);

    boolean refundPayment(String transactionId, BigDecimal amount);

    record PaymentResult(String transactionId, String redirectUrl) {
        public PaymentResult(String transactionId) {
            this(transactionId, null);
        }

        public boolean requiresRedirect() {
            return redirectUrl != null && !redirectUrl.isBlank();
        }
    }
}
