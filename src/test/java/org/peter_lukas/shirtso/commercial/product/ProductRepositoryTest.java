package org.peter_lukas.shirtso.commercial.product;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductRepositoryTest {

    @Mock
    private ProductRepository testedProductRepository;

    private List <Product> testedProducts;

    @BeforeEach
    void setUp() {
        testedProducts = createTestProducts();
    }

    private List<Product> createTestProducts() {
        Product product1 = new Product();
        product1.setProductId(UUID.randomUUID());
        product1.setProductName("Bluza Sportowa");
        product1.setPrice(new BigDecimal("120.00"));
        product1.setStock(30);

        Product product2 = new Product();
        product2.setProductId(UUID.randomUUID());
        product2.setProductName("Garnitur Klasyczny");
        product2.setPrice(new BigDecimal("800.00"));
        product2.setStock(10);

        Product product3 = new Product();
        product3.setProductId(UUID.randomUUID());
        product3.setProductName("Koszula Elegancka");
        product3.setPrice(new BigDecimal("150.00"));
        product3.setStock(20);

        return List.of(product1, product2, product3);
    }

    @Test
    void shouldFindAllProductsFromDB() {
//        given:
        when(testedProductRepository.findAllBy()).thenReturn(testedProducts);

//        when:
        List<Product> products = testedProductRepository.findAllBy();

//        then:
        assertThat(products.size()).isEqualTo(3);
        verify(testedProductRepository).findAllBy();
    }

    @Test
    void shouldFindAllProductsFromDBWithPagination() {
//        given:
        Sort sortByName = Sort.by("productName").ascending();
        Pageable firstPage = PageRequest.of(0, 2, sortByName);
        Pageable secondPage = PageRequest.of(1, 2, sortByName);

        when(testedProductRepository.findAllBy(firstPage)).thenReturn(List.of(testedProducts.get(0), testedProducts.get(1)));
        when(testedProductRepository.findAllBy(secondPage)).thenReturn(List.of(testedProducts.get(2)));

//        when:
        List<Product> firstPageProducts = testedProductRepository.findAllBy(firstPage);
        List<Product> secondPageProducts = testedProductRepository.findAllBy(secondPage);

//        then:
        assertThat(firstPageProducts)
            .hasSize(2)
            .extracting(Product::getProductName)
            .containsExactly("Bluza Sportowa", "Garnitur Klasyczny");

        assertThat(secondPageProducts)
                .hasSize(1)
                .extracting(Product::getProductName)
                .containsExactly("Koszula Elegancka");
    }
}
//TODO insert all categories and change test data for mens clothes