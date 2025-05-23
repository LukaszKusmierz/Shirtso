package org.peter_lukas.shirtso.commercial.product.image;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.peter_lukas.shirtso.commercial.product.Product;
import org.peter_lukas.shirtso.commercial.product.ProductRepository;
import org.peter_lukas.shirtso.commercial.product.image.dto.AssociateImageRequestDto;
import org.peter_lukas.shirtso.commercial.product.image.dto.CreateImageRequestDto;
import org.peter_lukas.shirtso.commercial.product.image.dto.ProductImageDto;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductImageServiceTest {

    @Mock
    private ProductRepository testedProductRepository;

    @Mock
    private ProductImageRepository testedImageRepository;

    @Mock
    private ProductImageMappingRepository testedImageMappingRepository;

    @Mock
    private ProductImageMapper testedImageMapper;

    @InjectMocks
    private ProductImageService testedProductImageService;

    private UUID testProductId;
    private Long testImageId;
    private Product testProduct;
    private ProductImage testImage;
    private ProductImageMapping testMapping;
    private ProductImageDto testImageDto;

    @BeforeEach
    void setUp() {
        testProductId = UUID.randomUUID();
        testImageId = 1L;

        testProduct = createTestProduct();
        testImage = createTestImage();
        testMapping = createTestMapping();
        testImageDto = createTestImageDto();
    }

    private Product createTestProduct() {
        Product product = new Product();
        product.setProductId(testProductId);
        product.setProductName("Test Product");
        return product;
    }

    private ProductImage createTestImage() {
        ProductImage image = new ProductImage();
        image.setImageId(testImageId);
        image.setImageUrl("https://example.com/test.jpg");
        image.setAltText("Test Image");
        return image;
    }

    private ProductImageMapping createTestMapping() {
        ProductImageMapping mapping = new ProductImageMapping();
        mapping.setProduct(testProduct);
        mapping.setImage(testImage);
        mapping.setPrimary(true);
        mapping.setDisplayOrder(1);
        return mapping;
    }

    private ProductImageDto createTestImageDto() {
        return new ProductImageDto(
                testImageId,
                "https://example.com/test.jpg",
                "Test Image",
                true,
                1
        );
    }

    @Test
    void getProductImages_WhenProductExists_ReturnsImageList() {
        // given
        when(testedImageMappingRepository.findByProduct_ProductIdOrderByDisplayOrderAsc(testProductId))
                .thenReturn(List.of(testMapping));
        when(testedImageMapper.mapToProductImageDto(testMapping)).thenReturn(testImageDto);

        // when
        List<ProductImageDto> result = testedProductImageService.getProductImages(testProductId);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testImageDto);
        verify(testedImageMappingRepository).findByProduct_ProductIdOrderByDisplayOrderAsc(testProductId);
    }

    @Test
    void getProductPrimaryImage_WhenPrimaryImageExists_ReturnsImage() {
        // given
        when(testedImageMappingRepository.findByProduct_ProductIdAndIsPrimaryTrue(testProductId))
                .thenReturn(Optional.of(testMapping));
        when(testedImageMapper.mapToProductImageDto(testMapping)).thenReturn(testImageDto);

        // when
        Optional<ProductImageDto> result = testedProductImageService.getProductPrimaryImage(testProductId);

        // then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testImageDto);
        assertThat(result.get().isPrimary()).isTrue();
    }

    @Test
    void associateImageWithProduct_WhenValidData_ReturnsMappedImage() {
        // given
        Long newImageId = 2L;
        ProductImage newImage = createTestImage();
        newImage.setImageId(newImageId);

        AssociateImageRequestDto request = new AssociateImageRequestDto(newImageId, true, 2);
        ProductImageMapping newMapping = new ProductImageMapping();
        ProductImageDto expectedDto = new ProductImageDto(newImageId, "url", "alt", true, 2);

        when(testedProductRepository.findById(testProductId)).thenReturn(Optional.of(testProduct));
        when(testedImageRepository.findById(newImageId)).thenReturn(Optional.of(newImage));
        when(testedImageMappingRepository.save(any(ProductImageMapping.class))).thenReturn(newMapping);
        when(testedImageMapper.mapToProductImageDto(newMapping)).thenReturn(expectedDto);

        // when
        ProductImageDto result = testedProductImageService.associateImageWithProduct(testProductId, request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.imageId()).isEqualTo(newImageId);
        assertThat(result.isPrimary()).isTrue();
        verify(testedImageMappingRepository).save(any(ProductImageMapping.class));
    }

    @Test
    void createImage_WithValidRequest_ReturnsCreatedImage() {
        // given
        CreateImageRequestDto request = new CreateImageRequestDto(
                "https://example.com/created.jpg",
                "Newly created image"
        );
        ProductImage createdImage = new ProductImage();
        createdImage.setImageId(99L);
        createdImage.setImageUrl(request.imageUrl());
        createdImage.setAltText(request.altText());

        when(testedImageRepository.save(any(ProductImage.class))).thenReturn(createdImage);

        // when
        ProductImage result = testedProductImageService.createImage(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getImageUrl()).isEqualTo(request.imageUrl());
        assertThat(result.getAltText()).isEqualTo(request.altText());
        verify(testedImageRepository).save(any(ProductImage.class));
    }

    @Test
    void updateImage_WhenImageExists_UpdatesAndReturnsImage() {
        // given
        String updatedUrl = "https://example.com/updated.jpg";
        String updatedAltText = "Updated alt text";
        CreateImageRequestDto request = new CreateImageRequestDto(updatedUrl, updatedAltText);

        ProductImage updatedImage = new ProductImage();
        updatedImage.setImageId(testImageId);
        updatedImage.setImageUrl(updatedUrl);
        updatedImage.setAltText(updatedAltText);

        when(testedImageRepository.findById(testImageId)).thenReturn(Optional.of(testImage));
        when(testedImageRepository.save(testImage)).thenReturn(updatedImage);

        // when
        ProductImage result = testedProductImageService.updateImage(testImageId, request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getImageUrl()).isEqualTo(updatedUrl);
        assertThat(result.getAltText()).isEqualTo(updatedAltText);
        verify(testedImageRepository).save(testImage);
    }

    @Test
    void deleteImage_WhenImageNotUsed_DeletesImage() {
        // given
        testImage.setProductMappings(Collections.emptySet());
        when(testedImageRepository.findById(testImageId)).thenReturn(Optional.of(testImage));

        // when
        testedProductImageService.deleteImage(testImageId);

        // then
        verify(testedImageRepository).delete(testImage);
    }

    @Test
    void deleteImage_WhenImageInUse_ThrowsException() {
        // given
        testImage.setProductMappings(Set.of(testMapping));

        when(testedImageRepository.findById(testImageId)).thenReturn(Optional.of(testImage));

        // when & then
        assertThatThrownBy(() -> testedProductImageService.deleteImage(testImageId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot delete image that is used by products");

        verify(testedImageRepository, never()).delete(any());
    }

    @Test
    void updatePrimaryImageStatus_WhenValidData_UpdatesPrimaryImage() {
        // given
        ProductImageMapping currentPrimary = new ProductImageMapping();
        currentPrimary.setPrimary(true);

        ProductImageMapping newPrimary = new ProductImageMapping();
        newPrimary.setImage(testImage);
        newPrimary.setPrimary(false);

        ProductImageMappingId mappingId = new ProductImageMappingId(testProductId, testImageId);

        when(testedImageMappingRepository.findByProduct_ProductIdAndIsPrimaryTrue(testProductId))
                .thenReturn(Optional.of(currentPrimary));
        when(testedImageMappingRepository.findById(mappingId))
                .thenReturn(Optional.of(newPrimary));

        // when
        testedProductImageService.updatePrimaryImageStatus(testProductId, testImageId);

        // then
        verify(testedImageMappingRepository, times(2)).save(any(ProductImageMapping.class));
        assertThat(currentPrimary.isPrimary()).isFalse();
        assertThat(newPrimary.isPrimary()).isTrue();
    }
}

