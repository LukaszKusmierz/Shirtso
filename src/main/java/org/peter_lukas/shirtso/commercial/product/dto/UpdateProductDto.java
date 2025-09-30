package org.peter_lukas.shirtso.commercial.product.dto;

import jakarta.validation.constraints.*;
import org.peter_lukas.shirtso.commercial.product.Currencies;
import org.peter_lukas.shirtso.commercial.product.Sizes;

import java.math.BigDecimal;

public record UpdateProductDto(
        @NotBlank(message = "This field can not be empty")
        @NotNull
        @Size(min = 3, max = 20, message = "Title must be between 3 and 20 characters")
        String productName,

        @NotBlank(message = "Description cannot be empty")
        String description,

        @NotNull(message = "Price cannot be empty")
        @DecimalMin(value = "0.01", message = "Price must be greater than 0")
        BigDecimal price,

        @NotNull(message = "Currency cannot be empty")
        Currencies currency,

        @NotNull(message = "Category cannot be empty")
        int subcategoryId,

        @NotBlank(message = "Supplier field cannot be empty")
        String supplier,

        @NotNull(message = "Quantity cannot be empty")
        @Min(value = 0, message = "Stock cannot be negative")
        long stock,

        @NotNull(message = "Size cannot be empty")
        Sizes size
) {
}
