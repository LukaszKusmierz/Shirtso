package org.peter_lukas.shirtso.commercial.product.validation;

public class PromoCodeNotFoundException extends RuntimeException {
    public PromoCodeNotFoundException(String message) {
        super(message);
    }
}
