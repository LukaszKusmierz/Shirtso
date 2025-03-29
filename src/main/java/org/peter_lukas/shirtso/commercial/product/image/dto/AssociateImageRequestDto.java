package org.peter_lukas.shirtso.commercial.product.image.dto;

import jakarta.validation.constraints.NotNull;

public record AssociateImageRequestDto(

        @NotNull(message = "Image ID can not be null")
        Integer imageId,

        boolean isPrimary,

        int displayOrder
) {
}
