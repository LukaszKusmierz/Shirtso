package org.peter_lukas.shirtso.commercial.discount.dto;

import org.peter_lukas.shirtso.commercial.discount.DiscountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PromoCodeDto(
        Integer promoCodeId,
        String code,
        String description,
        DiscountType discountType,
        BigDecimal discountValue,
        BigDecimal minimumOrderValue,
        BigDecimal maximumDiscountAmount,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Integer usageLimit,
        Integer usageCount,
        boolean isActive
) {
}
