package org.peter_lukas.shirtso.commercial.product.validation;

public class ImageNotFoundException extends RuntimeException {
    public ImageNotFoundException(String message) {
        super(message);
    }
}
