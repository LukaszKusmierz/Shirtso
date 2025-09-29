package org.peter_lukas.shirtso.commercial.product.validation;

public class ImageInUseException extends RuntimeException {
    public ImageInUseException(String message) {
        super(message);
    }
}
