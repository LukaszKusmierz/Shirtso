package org.peter_lukas.shirtso.product;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

@DataJpaTest
class ProductRepositoryTest {

    @Autowired
    private ProductRepository testedProductRepository;

    @Test
    void shouldReadProductsFromDB() {
//        given:

//        when:
        List<Product> products = testedProductRepository.findAllBy();


//        then:
    }
}