package org.peter_lukas.shirtso.commercial.unsplash;

import jakarta.annotation.security.RolesAllowed;
import org.peter_lukas.shirtso.analytics.LogExecutionTime;
import org.peter_lukas.shirtso.commercial.product.image.ProductImage;
import org.peter_lukas.shirtso.commercial.product.image.ProductImageService;
import org.peter_lukas.shirtso.commercial.product.image.dto.CreateImageRequestDto;
import org.peter_lukas.shirtso.commercial.unsplash.dto.UnsplashPhotoDto;
import org.peter_lukas.shirtso.commercial.unsplash.dto.UnsplashSearchResultDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

import static org.peter_lukas.shirtso.auth.config.SpringSecurityConfig.USER_WRITE;

@RestController
@RequestMapping("/api/unsplash")
public class UnsplashController {

    private final UnsplashService unsplashService;
    private final ProductImageService productImageService;

    public UnsplashController(UnsplashService unsplashService, ProductImageService productImageService) {
        this.unsplashService = unsplashService;
        this.productImageService = productImageService;
    }

    @GetMapping("/search")
    @LogExecutionTime
    public ResponseEntity<UnsplashSearchResultDto> searchPhotos(
            @RequestParam String query,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int perPage) {
        try {
            UnsplashSearchResultDto result = unsplashService.searchPhotos(query, page, perPage);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/photos/{photoId}")
    @LogExecutionTime
    public ResponseEntity<UnsplashPhotoDto> getPhoto(@PathVariable String photoId) {
        try {
            UnsplashPhotoDto photo = unsplashService.getPhoto(photoId);
            return ResponseEntity.ok(photo);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/photos/{photoId}/save")
    @LogExecutionTime
    @RolesAllowed(USER_WRITE)
    public ResponseEntity<ProductImage> savePhotoAsProductImage(@PathVariable String photoId) {
        try {
            // Get photo details from Unsplash
            UnsplashPhotoDto photo = unsplashService.getPhoto(photoId);

            // Track download as required by Unsplash API guidelines
            if (photo.links() != null && photo.links().downloadLocation() != null) {
                unsplashService.trackDownload(photo.links().downloadLocation());
            }

            // Create image in database using regular size URL
            CreateImageRequestDto createImageRequest = new CreateImageRequestDto(
                    photo.urls().regular(),
                    photo.altDescription() != null ? photo.altDescription() : photo.description()
            );

            ProductImage savedImage = productImageService.createImageIfNotExists(createImageRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedImage);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
