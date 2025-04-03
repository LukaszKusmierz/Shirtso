package org.peter_lukas.shirtso.commercial.order.dto;

import jakarta.validation.constraints.NotNull;

public record CreateOrderRequestDto(
        @NotNull(message = "Cart ID cannot be null")
        Integer cartId,
        Integer shippingMethodId,
        Integer addressId,
        String promoCode
) {
}
