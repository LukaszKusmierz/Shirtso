package org.peter_lukas.shirtso.commercial.order.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemDto(
        Integer orderItemId,
        UUID productId,
        String productName,
        Integer quantity,
        BigDecimal price,
        BigDecimal total
) {
}
