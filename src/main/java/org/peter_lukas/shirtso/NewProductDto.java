package org.peter_lukas.shirtso;

import jakarta.validation.constraints.*;

import java.math.*;

public record NewProductDto(
        @NotBlank(message = "This field can't be empty.")
        @NotNull
        @Size(min = 3, max = 20, message = "Title must be between 3 and 20 characters")
        String productName,
        @NotBlank(message = "Description cannot be empty")
        String description,
        @NotNull(message = "Price cannot be empty")
        BigDecimal price,
        @NotBlank(message = "Currency cannot be empty")
        String currency,
        String imageUrl,
        @NotBlank(message = "Category cannot be empty")
        String category,
        @NotBlank(message = "Supplier field cannot be empty")
        String supplier,
        @NotNull(message = "Quantity cannot be empty")
        long quantity,
        @NotNull
        Sizes size
) {
}
