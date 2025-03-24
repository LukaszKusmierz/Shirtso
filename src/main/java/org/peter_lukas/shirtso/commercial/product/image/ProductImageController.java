package org.peter_lukas.shirtso.commercial.product.image;

import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import org.peter_lukas.shirtso.analytics.LogExecutionTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static org.peter_lukas.shirtso.auth.config.SpringSecurityConfig.USER_WRITE;

@RestController
@RequestMapping("/api/images")
public class ProductImageController {

    private final ProductImageService productImageService;

    public ProductImageController(ProductImageService productImageService) {
        this.productImageService = productImageService;
    }

    @GetMapping
    @LogExecutionTime
    public List<ProductImage> getAllImages() {
        return productImageService.getAllImages();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @LogExecutionTime
    @RolesAllowed(USER_WRITE)
    public ProductImage createImage(@Valid @RequestBody CreateImageRequest request) {
        return productImageService.createImage(request);
    }

    @GetMapping("/{imageId}")
    @LogExecutionTime
    public ResponseEntity<ProductImage> getImageById(@PathVariable int imageId) {
        return productImageService.getImageById(imageId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{imageId}")
    @LogExecutionTime
    @RolesAllowed(USER_WRITE)
    public ResponseEntity<ProductImage> updateImage(
            @PathVariable int imageId,
            @Valid @RequestBody CreateImageRequest request) {
        return ResponseEntity.ok(productImageService.updateImage(imageId, request));
    }

    @DeleteMapping("/{imageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @LogExecutionTime
    @RolesAllowed(USER_WRITE)
    public void deleteImage(@PathVariable int imageId) {
        productImageService.deleteImage(imageId);
    }

    @GetMapping("/{imageId}/products")
    @LogExecutionTime
    public List<UUID> getProductsUsingImage(@PathVariable int imageId) {
        return productImageService.getProductsUsingImage(imageId);
    }
}
