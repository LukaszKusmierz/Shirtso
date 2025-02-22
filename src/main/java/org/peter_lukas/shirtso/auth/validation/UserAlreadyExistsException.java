package org.peter_lukas.shirtso.auth.validation;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String login) {
        super("User with username " + login + " already exists");
    }
}
