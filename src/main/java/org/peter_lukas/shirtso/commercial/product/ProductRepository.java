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

    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.imageMappings im LEFT JOIN FETCH im.image")
    List<Product> findAllWithImages();

    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.imageMappings im LEFT JOIN FETCH im.image")
    List<Product> findAllWithImages(Pageable pageable);

    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.imageMappings im LEFT JOIN FETCH im.image WHERE p.subcategoryId = :subcategoryId")
    List<Product> findAllBySubcategoryIdWithImages(int subcategoryId);

    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.imageMappings im LEFT JOIN FETCH im.image WHERE p.size = :size")
    List<Product> findAllBySizeWithImages(Sizes size);

    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.imageMappings im LEFT JOIN FETCH im.image WHERE p.stock > 0")
    List<Product> findAllInStockWithImages();

    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.imageMappings im LEFT JOIN FETCH im.image WHERE p.stock = 0")
    List<Product> findAllZeroStockWithImages();

    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.imageMappings im LEFT JOIN FETCH im.image WHERE p.stock > 0 AND p.stock < 3")
    List<Product> findAllLessThan3StockWithImages();

    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.imageMappings im LEFT JOIN FETCH im.image WHERE p.productName = :productName")
    List<Product> findAllByProductNameWithImages(String productName);

    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.imageMappings im LEFT JOIN FETCH im.image WHERE p.size = :size AND p.subcategoryId = :subcategoryId")
    List<Product> findProductsBySizeAndSubcategoryIdWithImages(Sizes size, int subcategoryId);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.imageMappings im LEFT JOIN FETCH im.image WHERE p.productId = :productId")
    Optional<Product> getProductByProductIdWithImages(UUID productId);

    @Query("SELECT DISTINCT p FROM Product p " +
            "LEFT JOIN FETCH p.imageMappings im " +
            "LEFT JOIN FETCH im.image " +
            "WHERE p.subcategoryId IN " +
            "(SELECT s.subcategoryId FROM Subcategory s WHERE s.category.categoryId = :categoryId)")
    List<Product> getProductsByCategoryIdWithImages(@Param("categoryId") int categoryId);

    List<Product> findAllBy();

    List<Product> findAllBy(Pageable pageable);

    List<Product> findAllBySubcategoryId(int subcategoryId);

    @Query("SELECT p FROM Product p WHERE p.size = :size")
    List<Product> findAllBySize(Sizes size);

    @Query("SELECT p FROM Product p WHERE p.stock > 0")
    List<Product> findAllInStock();

    @Query("SELECT p FROM Product p WHERE p.stock = 0")
    List<Product> findAllZeroStock();

    @Query("SELECT p FROM Product p WHERE p.stock > 0 AND p.stock < 3")
    List<Product> findAllLessThan3Stock();

    @Query("SELECT p FROM Product p WHERE p.productName = :productName")
    List<Product> findAllByProductName(String productName);

    @Query("SELECT COUNT(p) > 0 FROM Product p WHERE " +
            "(:productName IS NULL OR p.productName = :productName) AND " +
            "(:description IS NULL OR p.description = :description) AND " +
            "(:price IS NULL OR p.price = :price) AND " +
            "(:currency IS NULL OR p.currency = :currency) AND " +
            "(:imageId IS NULL OR p.imageId = :imageId) AND " +
            "(:subcategoryId IS NULL OR p.subcategoryId = :subcategoryId) AND " +
            "(:supplier IS NULL OR p.supplier = :supplier) AND " +
            "(:stock IS NULL OR p.stock = :stock) AND " +
            "(:size IS NULL OR p.size = :size)")
    boolean existsByAttributes(
            @Param("productName") String productName,
            @Param("description") String description,
            @Param("price") BigDecimal price,
            @Param("currency") Currencies currency,
            @Param("imageId") Long imageId,
            @Param("subcategoryId") Integer categoryId,
            @Param("supplier") String supplier,
            @Param("stock") Long stock,
            @Param("size") Sizes size
    );

    @Query("SELECT p FROM Product p WHERE p.size = :size AND p.subcategoryId = :subcategoryId")
    List<Product> findProductsBySizeAndSubcategoryId(Sizes size, int subcategoryId);

    @Query("SELECT p FROM Product p WHERE p.productId = :productId")
    Optional<Product> getProductByProductId(UUID productId);

    @Query("SELECT p FROM Product p WHERE p.subcategoryId IN " +
            "(SELECT s.subcategoryId FROM Subcategory s WHERE s.category.categoryId = :categoryId)")
    List<Product> getProductsByCategoryId(int categoryId);

    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.imageMappings im LEFT JOIN FETCH im.image WHERE p.size = :size AND p.subcategoryId IN " +
            "(SELECT s.subcategoryId FROM Subcategory s WHERE s.category.categoryId = :categoryId)")
    List<Product> findProductsBySizeAndCategoryIdWithImages(Sizes size, int categoryId);
}
