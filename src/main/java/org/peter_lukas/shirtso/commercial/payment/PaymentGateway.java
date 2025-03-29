package org.peter_lukas.shirtso.commercial.payment;

import java.math.BigDecimal;

public interface PaymentGateway {

    String processPayment(PaymentMethod method, BigDecimal amount, PaymentDetails details);
    boolean refundPayment(String transactionId, BigDecimal amount);
}
