package org.peter_lukas.shirtso.commercial.category;

import jakarta.validation.constraints.NotBlank;

public record NewCategoryDto(
        @NotBlank(message = "Category name can not be empty")
        String categoryName
) {
}
