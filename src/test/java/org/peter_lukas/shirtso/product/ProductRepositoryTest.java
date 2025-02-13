package org.peter_lukas.shirtso.product;



import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ProductRepositoryTest {

    @Autowired
    private ProductRepository testedProductRepository;

    @Test
    void shouldReadProductsFromDB() {
        //        when:
        List<Product> products = testedProductRepository.findAllBy();

//        then:
        assertThat(products.size()).isEqualTo(3);
    }
}