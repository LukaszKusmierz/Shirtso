package org.peter_lukas.shirtso.auth.registration;

public class UserNotFoundException extends Throwable {
    public UserNotFoundException(String massage) {
        super(massage);
    }
}
