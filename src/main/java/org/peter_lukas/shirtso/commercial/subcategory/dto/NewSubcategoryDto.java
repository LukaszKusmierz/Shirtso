package org.peter_lukas.shirtso.commercial.subcategory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record NewSubcategoryDto(
        @NotBlank(message = "Subcategory name can not be empty")
        String subcategoryName,
        @Min(value = 1, message = "Category id must be greater than 0")
        int categoryId
) {
}
