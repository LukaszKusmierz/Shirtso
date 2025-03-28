package org.peter_lukas.shirtso.commercial.product.validation;

public class CartItemNotFoundException extends RuntimeException{
    public CartItemNotFoundException(String message) { super(message); }
}
