package org.peter_lukas.shirtso.product;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    List<Product> findAllByOrderByProductNameAsc();

    List<Product> findAllByOrderByProductNameAsc(Pageable pageable);

    List<Product> findAllByCategoryId(int categoryId);

    List<Product> findAllBySize(Sizes size);
}
