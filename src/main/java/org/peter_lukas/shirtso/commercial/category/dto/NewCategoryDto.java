package org.peter_lukas.shirtso.commercial.category.dto;

import jakarta.validation.constraints.NotBlank;

public record NewCategoryDto(
        @NotBlank(message = "Category name can not be empty")
        String categoryName
) {
}
