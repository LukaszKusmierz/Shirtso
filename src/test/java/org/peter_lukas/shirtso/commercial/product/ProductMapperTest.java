package org.peter_lukas.shirtso.commercial.product;

import org.junit.jupiter.api.Test;
import org.peter_lukas.shirtso.commercial.product.dto.NewProductDto;
import org.peter_lukas.shirtso.commercial.product.dto.ProductDto;

import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;

class ProductMapperTest {

    private final ProductMapper testedProductMapper = new ProductMapper();

    @Test
    void shouldMapProductEntityToDto() {
//        given:

        Product entity = new Product("test-productname", "test-description",
                new BigDecimal(180), Currencies.PLN, 4L, 45, "test-supplier",
                50L, Sizes.XL);
//        when:

        ProductDto actualDto = testedProductMapper.mapProductEntityToDto(entity);
//        then:
        assertThat(actualDto).isNotNull();
        assertThat(actualDto.productId()).isEqualTo(entity.getProductId());
        assertThat(actualDto.productName()).isEqualTo(entity.getProductName());
        assertThat(actualDto.description()).isEqualTo(entity.getDescription());
        assertThat(actualDto.price()).isEqualTo(entity.getPrice());
        assertThat(actualDto.currency()).isEqualTo(entity.getCurrency());
        assertThat(actualDto.imageId()).isEqualTo(entity.getImageId());
        assertThat(actualDto.subcategoryId()).isEqualTo(entity.getSubcategoryId());
        assertThat(actualDto.supplier()).isEqualTo(entity.getSupplier());
        assertThat(actualDto.stock()).isEqualTo(entity.getStock());
        assertThat(actualDto.size()).isEqualTo(entity.getSize());
    }

    @Test
    void shouldMapNewProductDtoToEntity() {
//        given:

        NewProductDto dto = new NewProductDto("test-productname", "test-description",
                new BigDecimal(100), Currencies.EUR, 1L, 2, "test-supplier",
                50L, Sizes.S
        );
//        when:

        Product actualEntity = testedProductMapper.mapNewProductDtoToEntity(dto);
//        then:
        assertThat(actualEntity).isNotNull();
        assertThat(actualEntity.getProductName()).isEqualTo(dto.productName());
        assertThat(actualEntity.getDescription()).isEqualTo(dto.description());
        assertThat(actualEntity.getPrice()).isEqualTo(dto.price());
        assertThat(actualEntity.getCurrency()).isEqualTo(dto.currency());
        assertThat(actualEntity.getSize()).isEqualTo(dto.size());
        assertThat(actualEntity.getImageId()).isEqualTo(dto.imageId());
        assertThat(actualEntity.getSubcategoryId()).isEqualTo(dto.subcategoryId());
        assertThat(actualEntity.getSupplier()).isEqualTo(dto.supplier());
        assertThat(actualEntity.getStock()).isEqualTo(dto.stock());
        assertThat(actualEntity.getSize()).isEqualTo(dto.size());
    }
}