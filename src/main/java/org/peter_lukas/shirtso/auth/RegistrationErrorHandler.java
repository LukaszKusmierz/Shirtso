package org.peter_lukas.shirtso.auth;

import org.peter_lukas.shirtso.auth.validation.UserAlreadyExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RegistrationErrorHandler {

    @ExceptionHandler(UserAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleUsernameOccupied(UserAlreadyExistsException e) {
        return new ErrorResponse(e.getMessage());
    }

    public record ErrorResponse(String info) {}
}
