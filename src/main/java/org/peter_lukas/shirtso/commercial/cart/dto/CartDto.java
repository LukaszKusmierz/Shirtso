package org.peter_lukas.shirtso.commercial.cart.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CartDto(
        Integer cartId,
        UUID userId,
        List<CartItemDto> items,
        BigDecimal totalAmount,
        int totalItems
) {
}
