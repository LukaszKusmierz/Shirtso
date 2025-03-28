package org.peter_lukas.shirtso.commercial.order.dto;

import jakarta.validation.constraints.NotNull;

public record CreateOrderRequest(
        @NotNull(message = "Cart ID cannot be null")
        Integer cartId
) {
}
