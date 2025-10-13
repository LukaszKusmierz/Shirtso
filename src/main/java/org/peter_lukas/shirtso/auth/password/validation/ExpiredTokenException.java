package org.peter_lukas.shirtso.auth.password.validation;

public class ExpiredTokenException extends RuntimeException {
    public ExpiredTokenException(String message) {
        super(message);
    }
}
