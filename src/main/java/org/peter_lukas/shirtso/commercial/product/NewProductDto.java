package org.peter_lukas.shirtso.commercial.product;

import jakarta.validation.constraints.*;
import org.peter_lukas.shirtso.commercial.product.validation.UniqueProduct;

import java.math.*;

@UniqueProduct
public record NewProductDto(
        @NotBlank(message = "This field can't be empty.")
        @NotNull
        @Size(min = 3, max = 20, message = "Title must be between 3 and 20 characters")
        String productName,
        @NotBlank(message = "Description cannot be empty")
        String description,
        @NotNull(message = "Price cannot be empty")
        BigDecimal price,
        @NotNull(message = "Currency cannot be empty")
        Currencies currency,
        int imageId,
        @NotNull(message = "Category cannot be empty")
        int subcategoryId,
        @NotBlank(message = "Supplier field cannot be empty")
        String supplier,
        @NotNull(message = "Quantity cannot be empty")
        long stock,
        @NotNull
        Sizes size
) {
}
