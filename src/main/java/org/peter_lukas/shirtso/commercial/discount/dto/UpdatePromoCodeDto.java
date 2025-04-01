package org.peter_lukas.shirtso.commercial.discount.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.peter_lukas.shirtso.commercial.discount.DiscountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record UpdatePromoCodeDto(
        @NotBlank(message = "Description is required")
        String description,

        @NotNull(message = "Discount type is required")
        DiscountType discountType,

        @NotNull(message = "Discount value is required")
        @Positive(message = "Discount value must be positive")
        BigDecimal discountValue,

        BigDecimal minimumOrderValue,

        BigDecimal maximumDiscountAmount,

        @NotNull(message = "Start date is required")
        LocalDateTime startDate,

        @NotNull(message = "End date is required")
        LocalDateTime endDate,

        Integer usageLimit,

        boolean isActive
) {
}
