package org.peter_lukas.shirtso.commercial.product.image;

import jakarta.validation.constraints.NotBlank;

public record CreateImageRequest(

        @NotBlank(message = "Image URL can not be empty")
        String imageUrl,

        String altText
) {
}
