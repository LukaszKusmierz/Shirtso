package org.peter_lukas.shirtso.commercial.product.image;

public record ProductImageDto(
        long imageId,
        String imageUrl,
        String altText,
        boolean isPrimary,
        int displayOrder
) {
}
