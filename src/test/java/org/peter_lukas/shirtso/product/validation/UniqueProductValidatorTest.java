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
import static org.mockito.Mockito.verifyNoInteractions;

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
        assertThat(result).isTrue();
        verifyNoInteractions(productRepository);
    }

    @Test
    void shouldReturnTrueWhenProductDoesNotExistInRepository() {
//        given:

//        when:

//        then
    }
}
//TODO finish tests for validator