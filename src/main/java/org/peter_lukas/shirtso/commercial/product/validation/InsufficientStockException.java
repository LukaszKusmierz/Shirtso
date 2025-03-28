package org.peter_lukas.shirtso.commercial.product.validation;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String message) {
        super(message);
    }
}
