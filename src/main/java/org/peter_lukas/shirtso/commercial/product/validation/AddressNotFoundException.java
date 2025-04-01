package org.peter_lukas.shirtso.commercial.product.validation;

public class AddressNotFoundException extends RuntimeException {
    public AddressNotFoundException(String message) {
        super(message);
    }
}
