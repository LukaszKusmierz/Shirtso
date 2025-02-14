package org.peter_lukas.shirtso.commercial.product.validation;

public class ProductDuplicationException extends RuntimeException {
    public ProductDuplicationException(String message) {
        super(message);
    }
}
