package org.peter_lukas.shirtso;

import org.springframework.stereotype.*;

@Component
public class ProductMapper {
    public ProductDto mapProductEntityToDto(Product entity) {
        return new ProductDto(
                entity.getId(),
                entity.getProductName(),
                entity.getDescription(),
                entity.getPrice(),
                entity.getCurrency(),
                entity.getImageUrl(),
                entity.getCategory(),
                entity.getSupplier(),
                entity.getQuantity(),
                entity.getSize()
        );
    }

    public Product mapNewProductDtoToEntity(NewProductDto dto) {
        return new Product(
                dto.productName(),
                dto.description(),
                dto.price(),
                dto.currency(),
                dto.imageUrl(),
                dto.category(),
                dto.supplier(),
                dto.quantity(),
                dto.size()
        );
    }

}
