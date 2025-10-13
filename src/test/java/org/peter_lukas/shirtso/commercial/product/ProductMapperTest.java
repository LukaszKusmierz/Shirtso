package org.peter_lukas.shirtso.commercial.product;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.peter_lukas.shirtso.commercial.category.Category;
import org.peter_lukas.shirtso.commercial.product.dto.NewProductDto;
import org.peter_lukas.shirtso.commercial.product.dto.ProductDto;
import org.peter_lukas.shirtso.commercial.product.validation.SubcategoryNotFoundException;
import org.peter_lukas.shirtso.commercial.subcategory.Subcategory;
import org.peter_lukas.shirtso.commercial.subcategory.SubcategoryRepository;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductMapperTest {

    @Mock
    private SubcategoryRepository subcategoryRepository;

    private ProductMapper testedProductMapper;

    @BeforeEach
    void setUp() {
        testedProductMapper = new ProductMapper(subcategoryRepository);
    }

    @Test
    void shouldMapProductEntityToDto() {
        // given:
        Category category = new Category("Test Category");
        category.setCategoryId(5);

        Subcategory subcategory = new Subcategory("Test Subcategory", category);
        subcategory.setSubcategoryId(45);

        Product entity = new Product(
                "test-productname",
                "test-description",
                new BigDecimal(180),
                Currencies.PLN,
                subcategory,
                "test-supplier",
                50L,
                Sizes.XL
        );

        // when:
        ProductDto actualDto = testedProductMapper.mapProductEntityToDto(entity);

        // then:
        assertThat(actualDto).isNotNull();
        assertThat(actualDto.productId()).isEqualTo(entity.getProductId());
        assertThat(actualDto.productName()).isEqualTo(entity.getProductName());
        assertThat(actualDto.description()).isEqualTo(entity.getDescription());
        assertThat(actualDto.price()).isEqualTo(entity.getPrice());
        assertThat(actualDto.currency()).isEqualTo(entity.getCurrency());
        assertThat(actualDto.categoryId()).isEqualTo(5);
        assertThat(actualDto.subcategoryId()).isEqualTo(45);
        assertThat(actualDto.supplier()).isEqualTo(entity.getSupplier());
        assertThat(actualDto.stock()).isEqualTo(entity.getStock());
        assertThat(actualDto.size()).isEqualTo(entity.getSize());
    }

    @Test
    void shouldMapNewProductDtoToEntity() {
        // given:
        Category category = new Category("Test Category");
        category.setCategoryId(5);

        Subcategory subcategory = new Subcategory("Test Subcategory", category);
        subcategory.setSubcategoryId(45);

        NewProductDto dto = new NewProductDto(
                "test-productname",
                "test-description",
                new BigDecimal(180),
                Currencies.PLN,
                45,
                "test-supplier",
                50L,
                Sizes.XL
        );

        when(subcategoryRepository.findById(45)).thenReturn(Optional.of(subcategory));

        // when:
        Product entity = testedProductMapper.mapNewProductDtoToEntity(dto);

        // then:
        assertThat(entity).isNotNull();
        assertThat(entity.getProductName()).isEqualTo(dto.productName());
        assertThat(entity.getDescription()).isEqualTo(dto.description());
        assertThat(entity.getPrice()).isEqualTo(dto.price());
        assertThat(entity.getCurrency()).isEqualTo(dto.currency());
        assertThat(entity.getSubcategoryId()).isEqualTo(45);
        assertThat(entity.getCategoryId()).isEqualTo(5);
        assertThat(entity.getSupplier()).isEqualTo(dto.supplier());
        assertThat(entity.getStock()).isEqualTo(dto.stock());
        assertThat(entity.getSize()).isEqualTo(dto.size());
    }

    @Test
    void shouldMapNewProductDtoToEntity_throwsExceptionWhenSubcategoryNotFound() {
        // given:
        NewProductDto dto = new NewProductDto(
                "test-productname",
                "test-description",
                new BigDecimal(180),
                Currencies.PLN,
                999,
                "test-supplier",
                50L,
                Sizes.XL
        );

        when(subcategoryRepository.findById(999)).thenReturn(Optional.empty());

        // when & then:
        assertThat(org.assertj.core.api.Assertions.catchThrowable(() ->
                testedProductMapper.mapNewProductDtoToEntity(dto)))
                .isInstanceOf(SubcategoryNotFoundException.class)
                .hasMessageContaining("Subcategory not found: 999");
    }
}