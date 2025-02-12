package org.peter_lukas.shirtso.product.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.peter_lukas.shirtso.product.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;

@ExtendWith(MockitoExtension.class)
class UniqueProductValidatorTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ConstraintValidatorContext context;

    @InjectMocks
    private UniqueProductValidator validator;

    private NewProductDto testProductDto;

    @BeforeEach
    void setUp() {
        testProductDto = new NewProductDto("test-productname", "test-description",
                new BigDecimal(100), Currencies.EUR, 1, 2, "test-supplier",
                50L, Sizes.S
        );
    }

    @Test
    void shouldReturnTrueWhenNewProductDtoIsNull() {
//        given:

//        when:
        boolean result = validator.isValid(null, context);

//        then:
        assertThat(result).isFalse();
        verifyNoInteractions(productRepository);
    }

    @Test
    void shouldReturnTrueWhenProductDoesNotExistInRepository() {
//        given:
        when(productRepository.existsByAttributes(
                testProductDto.productName(),
                testProductDto.description(),
                testProductDto.price(),
                testProductDto.currency(),
                testProductDto.imageId(),
                testProductDto.categoryId(),
                testProductDto.supplier(),
                testProductDto.stock(),
                testProductDto.size()
        )).thenReturn(Boolean.FALSE);

//        when:
        boolean result = validator.isValid(testProductDto, context);

//        then
        assertThat(result).isTrue();
        verify(productRepository).existsByAttributes(
                testProductDto.productName(),
                testProductDto.description(),
                testProductDto.price(),
                testProductDto.currency(),
                testProductDto.imageId(),
                testProductDto.categoryId(),
                testProductDto.supplier(),
                testProductDto.stock(),
                testProductDto.size()
        );
    }

    @Test
    void shouldReturnFalseWhenProductAlreadyExistsInRepository() {
//        given:
        when(productRepository.existsByAttributes(
                testProductDto.productName(),
                testProductDto.description(),
                testProductDto.price(),
                testProductDto.currency(),
                testProductDto.imageId(),
                testProductDto.categoryId(),
                testProductDto.supplier(),
                testProductDto.stock(),
                testProductDto.size()
        )).thenReturn(Boolean.TRUE);

//        when:
        boolean result = validator.isValid(testProductDto, context);

//        then:
        assertThat(result).isFalse();
        verify(productRepository).existsByAttributes(
                testProductDto.productName(),
                testProductDto.description(),
                testProductDto.price(),
                testProductDto.currency(),
                testProductDto.imageId(),
                testProductDto.categoryId(),
                testProductDto.supplier(),
                testProductDto.stock(),
                testProductDto.size()
        );
    }
}
