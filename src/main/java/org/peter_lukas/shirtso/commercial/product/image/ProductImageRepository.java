package org.peter_lukas.shirtso.commercial.product.image;

import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    Optional<ProductImage> findByImageUrl(@NotBlank(message = "Image URL can not be empty") String s);
}
