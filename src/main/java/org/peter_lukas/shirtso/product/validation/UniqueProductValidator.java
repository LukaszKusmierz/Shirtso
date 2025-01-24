package org.peter_lukas.shirtso.product.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.peter_lukas.shirtso.product.NewProductDto;
import org.peter_lukas.shirtso.product.ProductRepository;

public class UniqueProductValidator implements ConstraintValidator<UniqueProduct, NewProductDto> {

    private final ProductRepository productRepository;

    public UniqueProductValidator(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public boolean isValid(NewProductDto newProductDto, ConstraintValidatorContext context) {
        if (newProductDto == null) {
            return true;
        }
        return !productRepository.existsByAttributes(
                newProductDto.productName(),
                newProductDto.description(),
                newProductDto.price(),
                newProductDto.currency(),
                newProductDto.imageId(),
                newProductDto.categoryId(),
                newProductDto.supplier(),
                newProductDto.stock(),
                newProductDto.size()
        );
    }
}
