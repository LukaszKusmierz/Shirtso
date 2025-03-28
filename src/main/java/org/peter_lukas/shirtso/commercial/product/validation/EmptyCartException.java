package org.peter_lukas.shirtso.commercial.product.validation;

public class EmptyCartException extends RuntimeException {
    public EmptyCartException(String message) { super(message); }
}
