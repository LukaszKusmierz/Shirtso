package org.peter_lukas.shirtso.commercial.discount.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import org.peter_lukas.shirtso.commercial.discount.DiscountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreatePromoCodeDto(
        @NotBlank(message = "Code is required")
        @Pattern(regexp = "^[A-Z0-9-_]{3,20}$", message = "Code must be 3-20 uppercase letters, numbers, hyphens or underscores")
        String code,

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

        Integer usageLimit
) {
}
