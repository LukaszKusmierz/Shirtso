package org.peter_lukas.shirtso.auth.password.validation;

public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException(String message) {
        super(message);
    }
}
