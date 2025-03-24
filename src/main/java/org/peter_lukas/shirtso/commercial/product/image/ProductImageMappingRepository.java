package org.peter_lukas.shirtso.commercial.product.image;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductImageMappingRepository extends JpaRepository<ProductImageMapping, ProductImageMappingId> {

    List<ProductImageMapping> findByProduct_ProductIdOrderByDisplayOrderAsc(UUID productId);

    Optional<ProductImageMapping> findByProduct_ProductIdAndIsPrimaryTrue(UUID productId);
}
