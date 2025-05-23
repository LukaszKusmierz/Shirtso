package org.peter_lukas.shirtso.commercial.product.image;

import org.junit.jupiter.api.Test;
import org.peter_lukas.shirtso.commercial.product.ProductRepository;
import org.peter_lukas.shirtso.commercial.product.image.dto.AssociateImageRequestDto;
import org.peter_lukas.shirtso.commercial.product.image.dto.CreateImageRequestDto;
import org.peter_lukas.shirtso.commercial.product.image.dto.ProductImageDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
class ProductImageServiceTest {

    @Autowired
    private ProductRepository testedProductRepository;

    @Autowired
    private ProductImageRepository testedImageRepository;

    @Autowired
    private ProductImageMappingRepository testedImageMappingRepository;

    @Autowired
    private ProductImageMapper testedImageMapper;

    @Autowired
    private ProductImageService testedProductImageService;

    private UUID testProductId;
    private Long testImageId;

//    @BeforeEach
//    void setUp() {
//        productImageService = new ProductImageService(
//                productRepository,
//                imageRepository,
//                imageMappingRepository,
//                imageMapper
//        );
//
//        // Get test data from H2 database (loaded by Flyway)
//        testProductId = UUID.fromString("11111111-aaaa-aaaa-aaaa-111111111111");
//        testImageId = 1L;
//    }

    @Test
    void getProductImages_WhenProductExists_ReturnsImageList() {
        // given - test data is loaded from V1_1__test-data.sql

        // when
        List<ProductImageDto> result = testedProductImageService.getProductImages(testProductId);

        // then
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).imageId()).isEqualTo(testImageId);
    }

    @Test
    void getProductPrimaryImage_WhenPrimaryImageExists_ReturnsImage() {
        // given - test data is loaded from V1_1__test-data.sql

        // when
        Optional<ProductImageDto> result = testedProductImageService.getProductPrimaryImage(testProductId);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().isPrimary()).isTrue();
    }

    @Test
    void associateImageWithProduct_WhenValidData_ReturnsMappedImage() {
        // given
        Long newImageId = 4L;
        ProductImage newImage = new ProductImage();
        newImage.setImageId(newImageId);
        newImage.setImageUrl("https://example.com/new-image.jpg");
        newImage.setAltText("New Test Image");
        testedImageRepository.save(newImage);

        AssociateImageRequestDto request = new AssociateImageRequestDto(newImageId, true, 2);

        // when
        ProductImageDto result = testedProductImageService.associateImageWithProduct(testProductId, request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.imageId()).isEqualTo(newImageId);
        assertThat(result.isPrimary()).isTrue();

        // Verify the mapping was saved
        List<ProductImageMapping> mappings = testedImageMappingRepository.findByProduct_ProductIdOrderByDisplayOrderAsc(testProductId);
        assertThat(mappings).hasSize(2); // Original + new mapping
    }

    @Test
    void createImage_WithValidRequest_ReturnsCreatedImage() {
        // given
        CreateImageRequestDto request = new CreateImageRequestDto(
                "https://example.com/created.jpg",
                "Newly created image"
        );

        // when
        ProductImage result = testedProductImageService.createImage(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getImageId()).isNotNull();
        assertThat(result.getImageUrl()).isEqualTo(request.imageUrl());
        assertThat(result.getAltText()).isEqualTo(request.altText());

        // Verify it was saved
        ProductImage savedImage = testedImageRepository.findById(result.getImageId()).orElse(null);
        assertThat(savedImage).isNotNull();
    }

    @Test
    void updateImage_WhenImageExists_UpdatesAndReturnsImage() {
        // given
        String updatedUrl = "https://example.com/updated-image.jpg";
        String updatedAltText = "Updated alt text";
        CreateImageRequestDto request = new CreateImageRequestDto(updatedUrl, updatedAltText);

        // when
        ProductImage result = testedProductImageService.updateImage(testImageId, request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getImageUrl()).isEqualTo(updatedUrl);
        assertThat(result.getAltText()).isEqualTo(updatedAltText);

        // Verify it was updated in the database
        ProductImage updatedImage = testedImageRepository.findById(testImageId).orElse(null);
        assertThat(updatedImage).isNotNull();
        assertThat(updatedImage.getImageUrl()).isEqualTo(updatedUrl);
    }

    @Test
    void deleteImage_WhenImageNotUsed_DeletesImage() {
        // given - create a new image that's not mapped to any product
        ProductImage newImage = new ProductImage();
        newImage.setImageUrl("https://example.com/to-delete.jpg");
        newImage.setAltText("To be deleted");
        ProductImage savedImage = testedImageRepository.save(newImage);

        // when
        testedProductImageService.deleteImage(savedImage.getImageId());

        // then
        Optional<ProductImage> deletedImage = testedImageRepository.findById(savedImage.getImageId());
        assertThat(deletedImage).isEmpty();
    }

    @Test
    void deleteImage_WhenImageInUse_ThrowsException() {
        // given - testImageId is already mapped to a product in the test data

        // when & then
        assertThatThrownBy(() -> testedProductImageService.deleteImage(testImageId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot delete image that is used by products");
    }

    @Test
    void updatePrimaryImageStatus_WhenValidData_UpdatesPrimaryImage() {
        // given - create and save a second image for the product
        ProductImage secondImage = new ProductImage();
        secondImage.setImageUrl("https://example.com/second-image.jpg");
        secondImage.setAltText("Second Image");
        secondImage = testedImageRepository.save(secondImage);

        // Associate the second image with the product (not primary)
        AssociateImageRequestDto associateRequest = new AssociateImageRequestDto(
                secondImage.getImageId(), false, 2);
        testedProductImageService.associateImageWithProduct(testProductId, associateRequest);

        // when - make the second image primary
        testedProductImageService.updatePrimaryImageStatus(testProductId, secondImage.getImageId());

        // then
        Optional<ProductImageMapping> primaryMapping = testedImageMappingRepository
                .findByProduct_ProductIdAndIsPrimaryTrue(testProductId);

        assertThat(primaryMapping).isPresent();
        assertThat(primaryMapping.get().getImage().getImageId()).isEqualTo(secondImage.getImageId());

        // Verify only one image is primary
        List<ProductImageMapping> allMappings = testedImageMappingRepository
                .findByProduct_ProductIdOrderByDisplayOrderAsc(testProductId);
        long primaryCount = allMappings.stream().filter(ProductImageMapping::isPrimary).count();
        assertThat(primaryCount).isEqualTo(1);
    }
}

