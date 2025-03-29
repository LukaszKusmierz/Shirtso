package org.peter_lukas.shirtso.commercial.product.image.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateImageRequestDto(

        @NotBlank(message = "Image URL can not be empty")
        String imageUrl,

        String altText
) {
}
