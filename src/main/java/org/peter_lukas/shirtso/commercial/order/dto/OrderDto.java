package org.peter_lukas.shirtso.commercial.order.dto;

import org.peter_lukas.shirtso.commercial.order.OrderStatus;
import org.peter_lukas.shirtso.commercial.shipping.dto.ShippingMethodDto;
import org.peter_lukas.shirtso.customer.dto.AddressDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderDto(
        Integer orderId,
        UUID userId,
        String userName,
        OrderStatus orderStatus,
        BigDecimal subtotalAmount,
        BigDecimal shippingAmount,
        BigDecimal discountAmount,
        BigDecimal taxAmount,
        BigDecimal totalAmount,
        String promoCode,
        ShippingMethodDto shippingMethod,
        AddressDto shippingAddress,
        LocalDateTime createdAt,
        List<OrderItemDto> items
) {
}
