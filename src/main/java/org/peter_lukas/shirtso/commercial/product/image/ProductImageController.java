package org.peter_lukas.shirtso.commercial.product.image;

import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import org.peter_lukas.shirtso.analytics.LogExecutionTime;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import static org.peter_lukas.shirtso.auth.config.SpringSecurityConfig.USER_WRITE;

@RestController
@RequestMapping("/api/images")
public class ProductImageController {

    private final ProductImageService productImageService;

    public ProductImageController(ProductImageService productImageService) {
        this.productImageService = productImageService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @LogExecutionTime
    @RolesAllowed(USER_WRITE)
    public ProductImage createImage(@Valid @RequestBody CreateImageRequest request) {
        return productImageService.createImage(request);
    }
}
