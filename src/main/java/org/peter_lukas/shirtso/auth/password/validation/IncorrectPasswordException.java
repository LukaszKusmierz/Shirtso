package org.peter_lukas.shirtso.auth.password.validation;

public class IncorrectPasswordException extends RuntimeException {
    public IncorrectPasswordException(String message) {
        super(message);
    }
}
