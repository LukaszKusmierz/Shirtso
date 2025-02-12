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
    private ProductRepository mockedProductRepository;

    @Mock
    private ConstraintValidatorContext mockedContext;

    @InjectMocks
    private UniqueProductValidator testedValidator;

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
        boolean result = testedValidator.isValid(null, mockedContext);

//        then:
        assertThat(result).isFalse();
        verifyNoInteractions(mockedProductRepository);
    }

    @Test
    void shouldReturnTrueWhenProductDoesNotExistInRepository() {
//        given:
        when(mockedProductRepository.existsByAttributes(
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
        boolean result = testedValidator.isValid(testProductDto, mockedContext);

//        then
        assertThat(result).isTrue();
        verify(mockedProductRepository).existsByAttributes(
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
        when(mockedProductRepository.existsByAttributes(
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
        boolean result = testedValidator.isValid(testProductDto, mockedContext);

//        then:
        assertThat(result).isFalse();
        verify(mockedProductRepository).existsByAttributes(
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
