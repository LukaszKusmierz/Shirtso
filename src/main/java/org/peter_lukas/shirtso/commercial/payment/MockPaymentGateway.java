package org.peter_lukas.shirtso.commercial.payment;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class MockPaymentGateway implements PaymentGateway {

    @Override
    public String processPayment(PaymentMethod method, BigDecimal amount, PaymentDetails details) {
        // In a real implementation, this would integrate with a payment provider
        // For testing, we'll simulate card validation and processing

        if (details.getCardNumber() != null && details.getCardNumber().equals("4111111111111111")) {
            // Test card that always succeeds
            return UUID.randomUUID().toString();
        } else if (details.getCardNumber() != null && details.getCardNumber().equals("4242424242424242")) {
            // Test card that always fails
            throw new RuntimeException("Payment declined by issuer");
        }

        // Simulate a payment process - in real life this would call a payment API
        // For demo purposes, we'll randomly succeed or fail based on odd/even seconds
        boolean success = System.currentTimeMillis() % 2 == 0;

        if (success) {
            // Generate a transaction ID
            return UUID.randomUUID().toString();
        } else {
            throw new RuntimeException("Payment processing failed");
        }
    }

    @Override
    public boolean refundPayment(String transactionId, BigDecimal amount) {
        // Simulate refund process
        return true;
    }
}
