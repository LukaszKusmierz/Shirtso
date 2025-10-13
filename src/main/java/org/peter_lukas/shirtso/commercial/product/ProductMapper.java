package org.peter_lukas.shirtso.commercial.product;

import org.hibernate.Hibernate;
import org.peter_lukas.shirtso.commercial.product.dto.NewProductDto;
import org.peter_lukas.shirtso.commercial.product.dto.ProductDto;
import org.peter_lukas.shirtso.commercial.product.image.dto.ProductImageDto;
import org.peter_lukas.shirtso.commercial.product.image.ProductImageMapping;
import org.peter_lukas.shirtso.commercial.product.validation.SubcategoryNotFoundException;
import org.peter_lukas.shirtso.commercial.subcategory.SubcategoryRepository;
import org.springframework.stereotype.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static org.peter_lukas.shirtso.messages.Alerts.SUBCATEGORY_NOT_FOUND;

@Component
public class ProductMapper {

    private final SubcategoryRepository subcategoryRepository;

    public ProductMapper(SubcategoryRepository subcategoryRepository) {
        this.subcategoryRepository = subcategoryRepository;
    }

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
                entity.getCategoryId(),
                entity.getSubcategoryId(),
                entity.getSupplier(),
                entity.getStock(),
                entity.getSize(),
                imageDtos
        );
    }

    public Product mapNewProductDtoToEntity(NewProductDto dto) {

        var subcategory = subcategoryRepository.findById(dto.subcategoryId())
                .orElseThrow(() -> new SubcategoryNotFoundException(SUBCATEGORY_NOT_FOUND + dto.subcategoryId()));

        return new Product(
                dto.productName(),
                dto.description(),
                dto.price(),
                dto.currency(),
                subcategory,
                dto.supplier(),
                dto.stock(),
                dto.size()
        );
    }

}
