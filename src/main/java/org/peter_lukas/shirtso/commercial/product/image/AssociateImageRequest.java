package org.peter_lukas.shirtso.commercial.product.image;

import jakarta.validation.constraints.NotNull;

public record AssociateImageRequest(

        @NotNull(message = "Image ID can not be null")
        Integer imageId,

        boolean isPrimary,

        int displayOrder
) {
}
