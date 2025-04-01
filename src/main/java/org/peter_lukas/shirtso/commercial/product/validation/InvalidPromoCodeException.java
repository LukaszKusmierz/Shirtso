package org.peter_lukas.shirtso.commercial.product.validation;

public class InvalidPromoCodeException extends RuntimeException {
    public InvalidPromoCodeException(String message) {
        super(message);
    }
}
