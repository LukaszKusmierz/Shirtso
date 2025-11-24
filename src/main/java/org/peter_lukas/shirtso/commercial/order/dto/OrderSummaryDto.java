package org.peter_lukas.shirtso.commercial.order.dto;

import org.peter_lukas.shirtso.commercial.order.OrderStatus;

public record OrderSummaryDto(
        Integer orderId,
        OrderStatus status,
        String date,
        String subtotal,
        String shipping,
        String discount,
        String tax,
        String total,
        String currency,
        int itemCount,
        String shippingMethodName,
        String shippingAddress
) {
}
