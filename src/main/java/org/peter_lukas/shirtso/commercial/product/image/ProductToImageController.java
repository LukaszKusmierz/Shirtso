package org.peter_lukas.shirtso.commercial.product.image;

import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import org.peter_lukas.shirtso.analytics.LogExecutionTime;
import org.peter_lukas.shirtso.commercial.product.image.dto.AssociateImageRequestDto;
import org.peter_lukas.shirtso.commercial.product.image.dto.ProductImageDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static org.peter_lukas.shirtso.auth.config.SpringSecurityConfig.USER_WRITE;

@RestController
@RequestMapping("/api/products/{productId}/images")
public class ProductToImageController {

    private final ProductImageService productImageService;

    public ProductToImageController(ProductImageService productImageService) {
        this.productImageService = productImageService;
    }

    @GetMapping
    @LogExecutionTime
    public List<ProductImageDto> getProductImages(@PathVariable UUID productId) {
        return productImageService.getProductImages(productId);
    }

    @GetMapping("/primary")
    @LogExecutionTime
    public ResponseEntity<ProductImageDto> getProductPrimaryImage(@PathVariable UUID productId) {
        return productImageService.getProductPrimaryImage(productId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @LogExecutionTime
    @RolesAllowed(USER_WRITE)
    public ProductImageDto associateImageWithProduct(
            @PathVariable UUID productId,
            @Valid @RequestBody AssociateImageRequestDto request) {
        return productImageService.associateImageWithProduct(productId, request);
    }

    @PutMapping("/primary/{imageId}")
    @LogExecutionTime
    @RolesAllowed(USER_WRITE)
    public ResponseEntity<Void> updatePrimaryImage(@PathVariable UUID productId,
           @PathVariable @jakarta.validation.constraints.NotNull(message = "Image ID can not be null") Long imageId) {
        productImageService.updatePrimaryImageStatus(productId, imageId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{imageId}")
    @LogExecutionTime
    @RolesAllowed(USER_WRITE)
    public ResponseEntity<Void> removeImageFromProduct(@PathVariable UUID productId, @PathVariable Long imageId) {
        productImageService.removeImageFromProduct(productId, imageId);
        return ResponseEntity.ok().build();
    }
}
