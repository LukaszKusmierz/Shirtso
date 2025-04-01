package org.peter_lukas.shirtso.commercial.discount.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record ValidatePromoCodeDto(
        @NotBlank(message = "Code is required")
        String code,

        @NotNull(message = "Order value is required")
        @PositiveOrZero(message = "Order value must be zero or positive")
        BigDecimal orderValue
) {
}
