package org.peter_lukas.shirtso.commercial.payment.payu;

public class PayUApiException extends RuntimeException {
    public PayUApiException(String message) {
        super(message);
    }

    public PayUApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
