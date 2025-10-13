package org.peter_lukas.shirtso.auth.password.validation;

public class UsedTokenException extends RuntimeException {
    public UsedTokenException(String message) {
        super(message);
    }
}
