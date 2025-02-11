package org.peter_lukas.shirtso.product;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ProductMapperTest {

    private final ProductMapper testedProductMapper = new ProductMapper();

    @Test
    void shouldMapNewProductDtoToEntity() {
//        given:

        NewProductDto dto = new NewProductDto("test-productname", "test-description",
                new BigDecimal(100), Currencies.EUR, 1, 2, "test-supplier",
                50L, Sizes.S
        );

//        when:

        Product entity = testedProductMapper.mapNewProductDtoToEntity(dto);

//        then:

        assertThat(entity).isNotNull();
        assertThat(entity.getProductName()).isEqualTo(dto.productName());
        assertThat(entity.getDescription()).isEqualTo(dto.description());
        assertThat(entity.getPrice()).isEqualTo(dto.price());
        assertThat(entity.getCurrency()).isEqualTo(dto.currency());
        assertThat(entity.getSize()).isEqualTo(dto.size());
        assertThat(entity.getImageId()).isEqualTo(dto.imageId());
        assertThat(entity.getCategoryId()).isEqualTo(dto.categoryId());
        assertThat(entity.getSupplier()).isEqualTo(dto.supplier());
        assertThat(entity.getStock()).isEqualTo(dto.stock());
        assertThat(entity.getSize()).isEqualTo(dto.size());
    }
}