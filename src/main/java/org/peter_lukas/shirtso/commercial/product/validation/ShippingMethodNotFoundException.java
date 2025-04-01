package org.peter_lukas.shirtso.commercial.product.validation;

public class ShippingMethodNotFoundException extends RuntimeException {
    public ShippingMethodNotFoundException(String message) {
        super(message);
    }
}
