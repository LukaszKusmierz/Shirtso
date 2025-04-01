package org.peter_lukas.shirtso.commercial.order.dto;

import org.peter_lukas.shirtso.commercial.order.OrderStatus;
import org.peter_lukas.shirtso.commercial.shipping.dto.ShippingMethodDto;

import java.math.BigDecimal;

public record OrderSummaryDto(
        Integer orderId,
        OrderStatus status,
        String date,
        BigDecimal subtotal,
        BigDecimal shipping,
        BigDecimal discount,
        BigDecimal tax,
        BigDecimal total,
        String promoCode,
        ShippingMethodDto shippingMethod,
        int itemCount
) {
}
