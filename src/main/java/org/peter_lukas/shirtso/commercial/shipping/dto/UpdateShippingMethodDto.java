package org.peter_lukas.shirtso.commercial.shipping.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record UpdateShippingMethodDto(
        @NotBlank(message = "Name is required")
        String name,

        @NotBlank(message = "Description is required")
        String description,

        @NotNull(message = "Price is required")
        @Positive(message = "Price must be positive")
        BigDecimal price,

        @NotNull(message = "Estimated delivery days is required")
        @Min(value = 1, message = "Estimated delivery days must be at least 1")
        Integer estimatedDeliveryDays,

        boolean isActive
) {
}
