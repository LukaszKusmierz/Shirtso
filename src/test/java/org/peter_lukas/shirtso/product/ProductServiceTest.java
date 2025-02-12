package org.peter_lukas.shirtso.product;

import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository mockedRepository;

    @Mock
    private ProductMapper mockedMapper;

    @InjectMocks
    private ProductService testedProductService;

    private Product testProduct1 = Instancio.create(Product.class);
    private Product testProduct2 = Instancio.create(Product.class);

    private ProductDto testProductDto1 = Instancio.create(ProductDto.class);
    private ProductDto testProductDto2 = Instancio.create(ProductDto.class);

    private List<Product> testProducts;
    private List<ProductDto> testProductDtos;

    @BeforeEach
    void setUp() {
        testProduct1 = Instancio.create(Product.class);
        testProduct2 = Instancio.create(Product.class);

        testProductDto1 = Instancio.create(ProductDto.class);
        testProductDto2 = Instancio.create(ProductDto.class);

        testProducts = List.of(testProduct1, testProduct2);
        testProductDtos = List.of(testProductDto1, testProductDto2);
    }


    @Test
    void shouldGetAllProducts() {
//        given:
        when(mockedRepository.findAllByOrderByProductNameAsc()).thenReturn(testProducts);
        when(mockedMapper.mapProductEntityToDto(testProduct1)).thenReturn(testProductDto1);
        when(mockedMapper.mapProductEntityToDto(testProduct2)).thenReturn(testProductDto2);

//        when:
        List<ProductDto> testedProductDtos = testedProductService.getAllProducts();

//        then:
        assertThat(testedProductDtos)
                .isNotNull()
                .isNotEmpty()
                .hasSize(testProductDtos.size())
                .isEqualTo(testProductDtos);

        verify(mockedRepository).findAllByOrderByProductNameAsc();
        verify(mockedMapper).mapProductEntityToDto(testProduct1);
        verify(mockedMapper).mapProductEntityToDto(testProduct2);
    }

    @Test
    void shouldGetAllProductsPage() {
//        given:
        Pageable pageable = mock(Pageable.class);

        when(mockedRepository.findAllByOrderByProductNameAsc(pageable)).thenReturn(testProducts);
        when(mockedMapper.mapProductEntityToDto(testProduct1)).thenReturn(testProductDto1);
        when(mockedMapper.mapProductEntityToDto(testProduct2)).thenReturn(testProductDto2);

//        when:
        List<ProductDto> testedProductDtos = testedProductService.getAllProductsPage(pageable);

//        then:
        assertThat(testedProductDtos)
                .isNotNull()
                .isNotEmpty()
                .hasSize(testProductDtos.size())
                .isEqualTo(testProductDtos);

        verify(mockedRepository).findAllByOrderByProductNameAsc(pageable);
    }
}