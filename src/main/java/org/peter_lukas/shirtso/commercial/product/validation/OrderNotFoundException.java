package org.peter_lukas.shirtso.commercial.product.validation;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(String message) { super(message); }
}
