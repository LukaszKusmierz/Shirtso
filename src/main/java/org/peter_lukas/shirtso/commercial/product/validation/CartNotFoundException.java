package org.peter_lukas.shirtso.commercial.product.validation;

public class CartNotFoundException extends RuntimeException {
    public CartNotFoundException(String message) { super(message); }
}
