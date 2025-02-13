package org.peter_lukas.shirtso.product;



import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ProductRepositoryTest {

    @Autowired
    private ProductRepository testedProductRepository;

    @Test
    void shouldFindAllProductsFromDB() {
        //        when:
        List<Product> products = testedProductRepository.findAllBy();

//        then:
        assertThat(products.size()).isEqualTo(3);
    }

    @Test
    void shouldFindAllProductsFromDBWithPagination() {
//        given:
        Sort sortByName = Sort.by("productName").ascending();
        Pageable firstPage = PageRequest.of(0, 2, sortByName);
        Pageable secondPage = PageRequest.of(1, 2, sortByName);

//        when:
        List<Product> firstPageProducts = testedProductRepository.findAllBy(firstPage);
        List<Product> secondPageProducts = testedProductRepository.findAllBy(secondPage);

//        then:
        assertThat(firstPageProducts)
            .hasSize(2)
            .extracting(Product::getProductName)
            .containsExactly("Laptop", "Novel");

        assertThat(secondPageProducts)
                .hasSize(1)
                .extracting(Product::getProductName)
                .containsExactly("T-Shirt");
    }
}
//TODO insert all categories and change test data for mens clothes