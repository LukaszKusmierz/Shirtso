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

    @Query("SELECT p FROM Product p WHERE p.stock > 0")
    List<Product> findAllInStock();

    @Query("SELECT p FROM Product p WHERE p.stock = 0")
    List<Product> findAllZeroStock();

    @Query("SELECT p FROM Product p WHERE p.stock > 0 AND p.stock < 3")
    List<Product> findAllLessThan3Stock();

    @Query("SELECT p FROM Product p WHERE p.productName = :productName")
    List<Product> findAllByProductName(String productName);
}
