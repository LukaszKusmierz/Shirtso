package org.peter_lukas.shirtso.commercial.product.image;

import org.springframework.stereotype.Component;

@Component
public class ProductImageMapper {
    public ProductImageDto mapToProductImageDto(ProductImageMapping entity) {
        return new ProductImageDto(
                entity.getImage().getImageId(),
                entity.getImage().getImageUrl(),
                entity.getImage().getAltText(),
                entity.isPrimary(),
                entity.getDisplayOrder()
        );
    }
}
