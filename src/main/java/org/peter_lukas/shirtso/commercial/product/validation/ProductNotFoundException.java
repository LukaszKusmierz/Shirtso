package org.peter_lukas.shirtso.commercial.product.validation;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(String message) {
        super(message);
    }
}
