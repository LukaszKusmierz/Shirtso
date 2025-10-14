package org.peter_lukas.shirtso.auth.validation;

public class UserNotFoundException extends Throwable {
    public UserNotFoundException(String massage) {
        super(massage);
    }
}
