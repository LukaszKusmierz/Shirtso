package org.peter_lukas.shirtso.product.validation;

public class DuplicationException extends RuntimeException {
    public DuplicationException(String message) {
        super(message);
    }
}
