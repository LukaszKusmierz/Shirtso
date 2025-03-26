package org.peter_lukas.shirtso.commercial.product;

import org.hibernate.Hibernate;
import org.peter_lukas.shirtso.commercial.product.image.ProductImageDto;
import org.peter_lukas.shirtso.commercial.product.image.ProductImageMapping;
import org.springframework.stereotype.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Component
public class ProductMapper {
    public ProductDto mapProductEntityToDto(Product entity) {

        List<ProductImageDto> imageDtos = new ArrayList<>();
        Set<ProductImageMapping> imageMappings = entity.getImageMappings();
        boolean isInitialized = Hibernate.isInitialized(imageMappings);

        if (isInitialized && entity.getImageMappings() != null && !entity.getImageMappings().isEmpty()) {
            imageDtos = entity.getImageMappings().stream()
                    .sorted(Comparator.comparing(ProductImageMapping::getDisplayOrder))
                    .map(mapping -> new ProductImageDto(
                            mapping.getImage().getImageId(),
                            mapping.getImage().getImageUrl(),
                            mapping.getImage().getAltText(),
                            mapping.isPrimary(),
                            mapping.getDisplayOrder()
                    ))
                    .toList();
        }
        return new ProductDto(
                entity.getProductId(),
                entity.getProductName(),
                entity.getDescription(),
                entity.getPrice(),
                entity.getCurrency(),
                entity.getImageId(),
                entity.getSubcategoryId(),
                entity.getSupplier(),
                entity.getStock(),
                entity.getSize(),
                imageDtos
        );
    }

    public Product mapNewProductDtoToEntity(NewProductDto dto) {
        return new Product(
                dto.productName(),
                dto.description(),
                dto.price(),
                dto.currency(),
                dto.imageId(),
                dto.subcategoryId(),
                dto.supplier(),
                dto.stock(),
                dto.size()
        );
    }

}
