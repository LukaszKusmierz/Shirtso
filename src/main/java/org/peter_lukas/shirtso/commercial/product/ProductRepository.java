package org.peter_lukas.shirtso.commercial.product;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    @Query("SELECT DISTINCT p FROM Product p " +
            "LEFT JOIN FETCH p.imageMappings im " +
            "LEFT JOIN FETCH im.image " +
            "LEFT JOIN FETCH p.subcategory s " +
            "LEFT JOIN FETCH s.category")
    List<Product> findAllWithImages();

    @Query("SELECT DISTINCT p FROM Product p " +
            "LEFT JOIN FETCH p.imageMappings im " +
            "LEFT JOIN FETCH im.image " +
            "LEFT JOIN FETCH p.subcategory s " +
            "LEFT JOIN FETCH s.category")
    List<Product> findAllWithImages(Pageable pageable);

    @Query("SELECT DISTINCT p FROM Product p " +
            "LEFT JOIN FETCH p.imageMappings im " +
            "LEFT JOIN FETCH im.image " +
            "LEFT JOIN FETCH p.subcategory s " +
            "LEFT JOIN FETCH s.category " +
            "WHERE p.subcategory.subcategoryId = :subcategoryId")
    List<Product> findAllBySubcategoryIdWithImages(@Param("subcategoryId") int subcategoryId);

    @Query("SELECT DISTINCT p FROM Product p " +
            "LEFT JOIN FETCH p.imageMappings im " +
            "LEFT JOIN FETCH im.image " +
            "LEFT JOIN FETCH p.subcategory s " +
            "LEFT JOIN FETCH s.category " +
            "WHERE p.size = :size")
    List<Product> findAllBySizeWithImages(@Param("size") Sizes size);

    @Query("SELECT DISTINCT p FROM Product p " +
            "LEFT JOIN FETCH p.imageMappings im " +
            "LEFT JOIN FETCH im.image " +
            "LEFT JOIN FETCH p.subcategory s " +
            "LEFT JOIN FETCH s.category " +
            "WHERE p.stock > 0")
    List<Product> findAllInStockWithImages();

    @Query("SELECT DISTINCT p FROM Product p " +
            "LEFT JOIN FETCH p.imageMappings im " +
            "LEFT JOIN FETCH im.image " +
            "LEFT JOIN FETCH p.subcategory s " +
            "LEFT JOIN FETCH s.category " +
            "WHERE p.stock = 0")
    List<Product> findAllZeroStockWithImages();

    @Query("SELECT DISTINCT p FROM Product p " +
            "LEFT JOIN FETCH p.imageMappings im " +
            "LEFT JOIN FETCH im.image " +
            "LEFT JOIN FETCH p.subcategory s " +
            "LEFT JOIN FETCH s.category " +
            "WHERE p.stock > 0 AND p.stock < 3")
    List<Product> findAllLessThan3StockWithImages();

    @Query("SELECT DISTINCT p FROM Product p " +
            "LEFT JOIN FETCH p.imageMappings im " +
            "LEFT JOIN FETCH im.image " +
            "LEFT JOIN FETCH p.subcategory s " +
            "LEFT JOIN FETCH s.category " +
            "WHERE p.productName = :productName")
    List<Product> findAllByProductNameWithImages(@Param("productName") String productName);

    @Query("SELECT DISTINCT p FROM Product p " +
            "LEFT JOIN FETCH p.imageMappings im " +
            "LEFT JOIN FETCH im.image " +
            "LEFT JOIN FETCH p.subcategory s " +
            "LEFT JOIN FETCH s.category " +
            "WHERE p.size = :size AND p.subcategory.subcategoryId = :subcategoryId")
    List<Product> findProductsBySizeAndSubcategoryIdWithImages(@Param("size") Sizes size,
                                                               @Param("subcategoryId") int subcategoryId);

    @Query("SELECT p FROM Product p " +
            "LEFT JOIN FETCH p.imageMappings im " +
            "LEFT JOIN FETCH im.image " +
            "LEFT JOIN FETCH p.subcategory s " +
            "LEFT JOIN FETCH s.category " +
            "WHERE p.productId = :productId")
    Optional<Product> getProductByProductIdWithImages(@Param("productId") UUID productId);

    @Query("SELECT DISTINCT p FROM Product p " +
            "LEFT JOIN FETCH p.imageMappings im " +
            "LEFT JOIN FETCH im.image " +
            "LEFT JOIN FETCH p.subcategory s " +
            "LEFT JOIN FETCH s.category " +
            "WHERE s.category.categoryId = :categoryId")
    List<Product> getProductsByCategoryIdWithImages(@Param("categoryId") int categoryId);

    @Query("SELECT DISTINCT p FROM Product p " +
            "LEFT JOIN FETCH p.imageMappings im " +
            "LEFT JOIN FETCH im.image " +
            "LEFT JOIN FETCH p.subcategory s " +
            "LEFT JOIN FETCH s.category " +
            "WHERE p.size = :size AND s.category.categoryId = :categoryId")
    List<Product> findProductsBySizeAndCategoryIdWithImages(@Param("size") Sizes size,
                                                            @Param("categoryId") int categoryId);


    @Query("SELECT COUNT(p) > 0 FROM Product p WHERE " +
            "(:productName IS NULL OR p.productName = :productName) AND " +
            "(:description IS NULL OR p.description = :description) AND " +
            "(:price IS NULL OR p.price = :price) AND " +
            "(:currency IS NULL OR p.currency = :currency) AND " +
            "(:subcategoryId IS NULL OR p.subcategory.subcategoryId = :subcategoryId) AND " +
            "(:supplier IS NULL OR p.supplier = :supplier) AND " +
            "(:stock IS NULL OR p.stock = :stock) AND " +
            "(:size IS NULL OR p.size = :size)")
    boolean existsByAttributes(
            @Param("productName") String productName,
            @Param("description") String description,
            @Param("price") BigDecimal price,
            @Param("currency") Currencies currency,
            @Param("subcategoryId") Integer subcategoryId,
            @Param("supplier") String supplier,
            @Param("stock") Long stock,
            @Param("size") Sizes size
    );

    List<Product> findAllBy();

    List<Product> findAllBy(Pageable pageable);
}
