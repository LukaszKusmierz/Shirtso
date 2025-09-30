package org.peter_lukas.shirtso.commercial.product.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.peter_lukas.shirtso.commercial.product.dto.NewProductDto;
import org.peter_lukas.shirtso.commercial.product.ProductRepository;

public class UniqueProductValidator implements ConstraintValidator<UniqueProduct, NewProductDto> {

    private final ProductRepository productRepository;

    public UniqueProductValidator(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public boolean isValid(NewProductDto newProductDto, ConstraintValidatorContext context) {

        return newProductDto != null &&
                !productRepository.existsByAttributes(
                newProductDto.productName(),
                newProductDto.description(),
                newProductDto.price(),
                newProductDto.currency(),
                newProductDto.subcategoryId(),
                newProductDto.supplier(),
                newProductDto.stock(),
                newProductDto.size()
        );
    }
}
