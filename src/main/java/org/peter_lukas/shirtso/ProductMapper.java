package org.peter_lukas.shirtso;

import org.springframework.stereotype.*;

@Component
public class ProductMapper {
    public ProductDto mapProductEntityToDto(Product entity) {
        return new ProductDto(
                entity.getProductId(),
                entity.getProductName(),
                entity.getDescription(),
                entity.getPrice(),
                entity.getCurrency(),
                entity.getImageId(),
                entity.getCategoryId(),
                entity.getSupplier(),
                entity.getStock(),
                entity.getSize()
        );
    }

    public Product mapNewProductDtoToEntity(NewProductDto dto) {
        return new Product(
                dto.productName(),
                dto.description(),
                dto.price(),
                dto.currency(),
                dto.imageId(),
                dto.categoryId(),
                dto.supplier(),
                dto.stock(),
                dto.size()
        );
    }

}
