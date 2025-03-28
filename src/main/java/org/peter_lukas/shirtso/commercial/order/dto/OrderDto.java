package org.peter_lukas.shirtso.commercial.order.dto;

import org.peter_lukas.shirtso.commercial.order.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderDto(
        Integer orderId,
        UUID userId,
        String userName,
        OrderStatus orderStatus,
        BigDecimal totalAmount,
        LocalDateTime createdAt,
        List<OrderItemDto> items
) {
}
