package org.peter_lukas.shirtso.commercial.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateCartItemDto(
        @NotNull(message = "Cart item ID cannot be null")
        Integer cartItemId,

        @NotNull(message = "Quantity cannot be null")
        @Min(value = 1, message = "Quantity must be at least 1")
        Integer quantity
) {
}
