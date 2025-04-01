package org.peter_lukas.shirtso.commercial.shipping.dto;

import java.math.BigDecimal;

public record ShippingMethodDto(
        Integer shippingMethodId,
        String name,
        String description,
        BigDecimal price,
        Integer estimatedDeliveryDays,
        boolean isActive
) {
}
