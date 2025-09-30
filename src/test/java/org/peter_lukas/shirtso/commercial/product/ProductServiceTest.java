package org.peter_lukas.shirtso.commercial.product;

import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.peter_lukas.shirtso.commercial.product.dto.NewProductDto;
import org.peter_lukas.shirtso.commercial.product.dto.ProductDto;
import org.peter_lukas.shirtso.messages.Alerts;
import org.peter_lukas.shirtso.commercial.product.validation.ProductDuplicationException;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
        when(mockedRepository.findAllWithImages()).thenReturn(testProducts);
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

        verify(mockedRepository).findAllWithImages();
        verify(mockedMapper).mapProductEntityToDto(testProduct1);
        verify(mockedMapper).mapProductEntityToDto(testProduct2);
    }

    @Test
    void shouldGetAllProductsPage() {
//        given:
        Pageable pageable = mock(Pageable.class);

        when(mockedRepository.findAllWithImages(pageable)).thenReturn(testProducts);
        when(mockedMapper.mapProductEntityToDto(testProduct1)).thenReturn(testProductDto1);
        when(mockedMapper.mapProductEntityToDto(testProduct2)).thenReturn(testProductDto2);

//        when:
        List<ProductDto> results = testedProductService.getAllProductsPage(pageable);

//        then:
        assertThat(results)
                .isNotNull()
                .isNotEmpty()
                .hasSize(testProductDtos.size())
                .isEqualTo(testProductDtos);

        verify(mockedRepository).findAllWithImages(pageable);
    }

    @Test
    void addNewProduct_Success() {
//        given:
        NewProductDto newProductDto = Instancio.create(NewProductDto.class);

        when(mockedRepository.existsByAttributes(
                newProductDto.productName(),
                newProductDto.description(),
                newProductDto.price(),
                newProductDto.currency(),
                newProductDto.subcategoryId(),
                newProductDto.supplier(),
                newProductDto.stock(),
                newProductDto.size()
        )).thenReturn(false);

        when(mockedMapper.mapNewProductDtoToEntity(newProductDto)).thenReturn(testProduct1);
        when(mockedRepository.save(testProduct1)).thenReturn(testProduct1);
        when(mockedMapper.mapProductEntityToDto(testProduct1)).thenReturn(testProductDto1);

//        when:
        ProductDto result = testedProductService.addNewProduct(newProductDto);

//        then:
        assertThat(result).isEqualTo(testProductDto1);
    }

    @Test
    void addNewProduct_Failure() {
//        given:
        NewProductDto newProductDto = Instancio.create(NewProductDto.class);

        when(mockedRepository.existsByAttributes(
                newProductDto.productName(),
                newProductDto.description(),
                newProductDto.price(),
                newProductDto.currency(),
                newProductDto.subcategoryId(),
                newProductDto.supplier(),
                newProductDto.stock(),
                newProductDto.size()
        )).thenReturn(true);

//        when:
        assertThatThrownBy(() -> testedProductService.addNewProduct(newProductDto))
        .isInstanceOf(ProductDuplicationException.class)
        .hasMessage(Alerts.DUPLICATE_PRODUCT);
    }

    @Test
    void getProductsBySubcategoryId_ShouldReturnMappedDtos_WhenProductsFound() {
//        given:
        int categoryId = 1;

        when(mockedRepository.findAllBySubcategoryIdWithImages(categoryId)).thenReturn(testProducts);
        when(mockedMapper.mapProductEntityToDto(testProduct1)).thenReturn(testProductDto1);
        when(mockedMapper.mapProductEntityToDto(testProduct2)).thenReturn(testProductDto2);

//        when:
        List<ProductDto> productDtos = testedProductService.getProductsBySubcategoryId(categoryId);

//        then:
        assertThat(productDtos)
                .isNotNull()
                .hasSize(testProductDtos.size())
                .containsExactly(testProductDto1, testProductDto2);
    }

    @Test
    void getProductsBySubcategoryId_ShouldReturnEmptyList_WhenProductsNotFound() {
//        given:
        int categoryId = 4;

        when(mockedRepository.findAllBySubcategoryIdWithImages(categoryId)).thenReturn(Collections.emptyList());

//        when:
        List<ProductDto> productDtos = testedProductService.getProductsBySubcategoryId(categoryId);

//        then:
        assertThat(productDtos).isEmpty();
    }

    @Test
    void getProductsBySize_ShouldReturnMappedDtos_WhenProductsFound() {
//        given:
        Sizes size = Sizes.L;

        when(mockedRepository.findAllBySizeWithImages(size)).thenReturn(testProducts);
        when(mockedMapper.mapProductEntityToDto(testProduct1)).thenReturn(testProductDto1);
        when(mockedMapper.mapProductEntityToDto(testProduct2)).thenReturn(testProductDto2);

//        when:
        List<ProductDto> productDtos = testedProductService.getProductsBySize(size);

//        then:
        assertThat(productDtos)
                .isNotNull()
                .hasSize(testProductDtos.size())
                .containsExactly(testProductDto1, testProductDto2);
    }

    @Test
    void getProductsBySize_ShouldReturnEmptyList_WhenProductsNotFound() {
//        given:
        Sizes size = Sizes.M;

        when(mockedRepository.findAllBySizeWithImages(size)).thenReturn(Collections.emptyList());

//        when:
        List<ProductDto> productDtos = testedProductService.getProductsBySize(size);

//        then:
        assertThat(productDtos).isEmpty();
    }

    @Test
    void getProductsByCategoryId_ShouldReturnMappedDtos_WhenProductsFound() {
        // given:
        int categoryId = 1;

        when(mockedRepository.getProductsByCategoryIdWithImages(categoryId)).thenReturn(testProducts);
        when(mockedMapper.mapProductEntityToDto(testProduct1)).thenReturn(testProductDto1);
        when(mockedMapper.mapProductEntityToDto(testProduct2)).thenReturn(testProductDto2);

        // when:
        List<ProductDto> productDtos = testedProductService.getProductsByCategoryId(categoryId);

        // then:
        assertThat(productDtos)
                .isNotNull()
                .hasSize(testProductDtos.size())
                .containsExactly(testProductDto1, testProductDto2);
    }

    @Test
    void getProductsByCategoryId_ShouldReturnEmptyList_WhenProductsNotFound() {
        // given:
        int categoryId = 4;

        when(mockedRepository.getProductsByCategoryIdWithImages(categoryId)).thenReturn(Collections.emptyList());

        // when:
        List<ProductDto> productDtos = testedProductService.getProductsByCategoryId(categoryId);

        // then:
        assertThat(productDtos).isEmpty();
    }

    @Test
    void getProductsBySizeAndCategoryId_ShouldReturnMappedDtos_WhenProductsFound() {
        // given:
        Sizes size = Sizes.L;
        int categoryId = 1;

        when(mockedRepository.findProductsBySizeAndCategoryIdWithImages(size, categoryId)).thenReturn(testProducts);
        when(mockedMapper.mapProductEntityToDto(testProduct1)).thenReturn(testProductDto1);
        when(mockedMapper.mapProductEntityToDto(testProduct2)).thenReturn(testProductDto2);

        // when:
        List<ProductDto> productDtos = testedProductService.getProductsBySizeAndCategoryId(size, categoryId);

        // then:
        assertThat(productDtos)
                .isNotNull()
                .hasSize(testProductDtos.size())
                .containsExactly(testProductDto1, testProductDto2);
    }

    @Test
    void getProductsBySizeAndCategoryId_ShouldReturnEmptyList_WhenProductsNotFound() {
        // given:
        Sizes size = Sizes.M;
        int categoryId = 4;

        when(mockedRepository.findProductsBySizeAndCategoryIdWithImages(size, categoryId)).thenReturn(Collections.emptyList());

        // when:
        List<ProductDto> productDtos = testedProductService.getProductsBySizeAndCategoryId(size, categoryId);

        // then:
        assertThat(productDtos).isEmpty();
    }

    @Test
    void getProductsBySizeAndSubcategoryId_ShouldReturnMappedDtos_WhenProductsFound() {
        // given:
        Sizes size = Sizes.L;
        int subcategoryId = 1;

        when(mockedRepository.findProductsBySizeAndSubcategoryIdWithImages(size, subcategoryId)).thenReturn(testProducts);
        when(mockedMapper.mapProductEntityToDto(testProduct1)).thenReturn(testProductDto1);
        when(mockedMapper.mapProductEntityToDto(testProduct2)).thenReturn(testProductDto2);

        // when:
        List<ProductDto> productDtos = testedProductService.getProductsBySizeAndSubcategoryId(size, subcategoryId);

        // then:
        assertThat(productDtos)
                .isNotNull()
                .hasSize(testProductDtos.size())
                .containsExactly(testProductDto1, testProductDto2);
    }

    @Test
    void getProductsBySizeAndSubcategoryId_ShouldReturnEmptyList_WhenProductsNotFound() {
        // given:
        Sizes size = Sizes.M;
        int subcategoryId = 4;

        when(mockedRepository.findProductsBySizeAndSubcategoryIdWithImages(size, subcategoryId)).thenReturn(Collections.emptyList());

        // when:
        List<ProductDto> productDtos = testedProductService.getProductsBySizeAndSubcategoryId(size, subcategoryId);

        // then:
        assertThat(productDtos).isEmpty();
    }

    @Test
    void getProductsInStock_ShouldReturnMappedDtos_WhenProductsFound() {
//        given:
        when(mockedRepository.findAllInStockWithImages()).thenReturn(testProducts);
        when(mockedMapper.mapProductEntityToDto(testProduct1)).thenReturn(testProductDto1);
        when(mockedMapper.mapProductEntityToDto(testProduct2)).thenReturn(testProductDto2);

//        when:
        List<ProductDto> productDtos = testedProductService.getProductsInStock();

//        then:
        assertThat(productDtos)
                .isNotNull()
                .hasSize(testProductDtos.size())
                .containsExactly(testProductDto1, testProductDto2);
    }

    @Test
    void getProductsInStock_ShouldReturnEmptyList_WhenProductsNotFound() {
//        given:
        when(mockedRepository.findAllInStockWithImages()).thenReturn(Collections.emptyList());

//        when:
        List<ProductDto> productDtos = testedProductService.getProductsInStock();

//        then:
        assertThat(productDtos).isEmpty();
    }

    @Test
    void getProductsNotInStock_ShouldReturnMappedDtos_WhenProductsFound() {
//        given:
        when(mockedRepository.findAllZeroStockWithImages()).thenReturn(testProducts);
        when(mockedMapper.mapProductEntityToDto(testProduct1)).thenReturn(testProductDto1);
        when(mockedMapper.mapProductEntityToDto(testProduct2)).thenReturn(testProductDto2);

//        when:
        List<ProductDto> productDtos = testedProductService.getProductsNotInStock();

//        then:
        assertThat(productDtos)
                .isNotNull()
                .hasSize(testProductDtos.size())
                .containsExactly(testProductDto1, testProductDto2);
    }

    @Test
    void getProductsNotInStock_ShouldReturnEmptyList_WhenProductsNotFound() {
//        given:
        when(mockedRepository.findAllZeroStockWithImages()).thenReturn(Collections.emptyList());

//        when:
        List<ProductDto> productDtos = testedProductService.getProductsNotInStock();

//        then:
        assertThat(productDtos).isEmpty();
    }

    @Test
    void getProductsTopUpStock_ShouldReturnMappedDtos_WhenProductsFound() {
//        given:
        when(mockedRepository.findAllLessThan3StockWithImages()).thenReturn(testProducts);
        when(mockedMapper.mapProductEntityToDto(testProduct1)).thenReturn(testProductDto1);
        when(mockedMapper.mapProductEntityToDto(testProduct2)).thenReturn(testProductDto2);

//        when:
        List<ProductDto> productDtos = testedProductService.getProductsTopUpStock();

//        then:
        assertThat(productDtos)
                .isNotNull()
                .hasSize(testProductDtos.size())
                .containsExactly(testProductDto1, testProductDto2);
    }

    @Test
    void getProductsTopUpStock_ShouldReturnEmptyList_WhenProductsNotFound() {
//        given:
        when(mockedRepository.findAllLessThan3StockWithImages()).thenReturn(Collections.emptyList());

//        when:
        List<ProductDto> productDtos = testedProductService.getProductsTopUpStock();

//        then:
        assertThat(productDtos).isEmpty();
    }

    @Test
    void getProductsByProductName_ShouldReturnMappedDtos_WhenProductsFound() {
//        given:
        String productName = "testProduct";

        when(mockedRepository.findAllByProductNameWithImages(productName)).thenReturn(testProducts);
        when(mockedMapper.mapProductEntityToDto(testProduct1)).thenReturn(testProductDto1);
        when(mockedMapper.mapProductEntityToDto(testProduct2)).thenReturn(testProductDto2);

//        when:
        List<ProductDto> productDtos = testedProductService.getProductsByProductName(productName);

//        then:
        assertThat(productDtos)
                .isNotNull()
                .hasSize(testProductDtos.size())
                .containsExactly(testProductDto1, testProductDto2);
    }

    @Test
    void getProductsByProductName_ShouldReturnEmptyList_WhenProductsNotFound() {
//        given:
        String productName = "testProduct";

        when(mockedRepository.findAllByProductNameWithImages(productName)).thenReturn(Collections.emptyList());

//        when:
        List<ProductDto> productDtos = testedProductService.getProductsByProductName(productName);

//        then:
        assertThat(productDtos).isEmpty();
    }
}
