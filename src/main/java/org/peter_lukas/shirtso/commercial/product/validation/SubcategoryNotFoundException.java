package org.peter_lukas.shirtso.commercial.product.validation;

public class SubcategoryNotFoundException extends RuntimeException {
    public SubcategoryNotFoundException(String message) {
        super(message);
    }
}
