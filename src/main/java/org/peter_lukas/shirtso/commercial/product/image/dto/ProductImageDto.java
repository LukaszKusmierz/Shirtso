package org.peter_lukas.shirtso.commercial.product.image.dto;

public record ProductImageDto(
        int imageId,
        String imageUrl,
        String altText,
        boolean isPrimary,
        int displayOrder
) {
}
