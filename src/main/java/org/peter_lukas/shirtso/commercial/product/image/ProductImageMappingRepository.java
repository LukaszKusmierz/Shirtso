package org.peter_lukas.shirtso.commercial.product.image;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductImageMappingRepository extends JpaRepository<ProductImageMapping, ProductImageMappingId> {

    @Query("SELECT pim FROM ProductImageMapping pim JOIN FETCH pim.image i WHERE " +
            "pim.product.productId = :productId ORDER BY pim.displayOrder ASC")
    List<ProductImageMapping> findByProduct_ProductIdOrderByDisplayOrderAsc(UUID productId);

    Optional<ProductImageMapping> findByProduct_ProductIdAndIsPrimaryTrue(UUID productId);
}
