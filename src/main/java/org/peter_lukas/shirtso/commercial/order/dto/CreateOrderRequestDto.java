package org.peter_lukas.shirtso.commercial.order.dto;

import jakarta.validation.constraints.NotNull;

public record CreateOrderRequestDto(
        @NotNull(message = "Cart ID cannot be null")
        Integer cartId,

        @NotNull(message = "Shipping method ID cannot be null")
                Integer shippingMethodId,

        @NotNull(message = "Shipping address ID cannot be null")
        Integer addressId,

        String promoCode
) {
}
