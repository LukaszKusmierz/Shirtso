package org.peter_lukas.shirtso.commercial.order.dto;

import jakarta.validation.constraints.NotNull;
import org.peter_lukas.shirtso.commercial.order.OrderStatus;

public record UpdateOrderStatusRequestDto(
        @NotNull(message = "Order status cannot be null")
        OrderStatus orderStatus
) {
}
