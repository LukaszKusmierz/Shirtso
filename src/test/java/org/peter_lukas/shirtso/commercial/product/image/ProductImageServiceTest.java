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
import org.peter_lukas.shirtso.commercial.product.validation.ImageAssociatedException;
import org.peter_lukas.shirtso.commercial.product.validation.ImageInUseException;
import org.peter_lukas.shirtso.commercial.product.validation.ImageNotFoundException;
import org.peter_lukas.shirtso.commercial.product.validation.ProductNotFoundException;

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
        ProductImage result = testedProductImageService.createImageIfNotExists(request);

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
                .isInstanceOf(ImageInUseException.class)
                .hasMessageContaining("Cannot delete image that is used by other products");

        verify(testedImageRepository, never()).delete(any());
    }

    @Test
    void associateImageWithProduct_WhenValidData_ReturnsMappedImage() {
        // given
        Long newImageId = 2L;
        ProductImage newImage = new ProductImage();
        newImage.setImageId(newImageId);
        newImage.setImageUrl("https://example.com/new.jpg");
        newImage.setAltText("New Image");

        AssociateImageRequestDto request = new AssociateImageRequestDto(newImageId, true, 2);

        ProductImageMapping expectedMapping = new ProductImageMapping(testProduct, newImage, true, 2);

        ProductImageDto expectedDto = new ProductImageDto(
                newImageId,
                "https://example.com/new.jpg",
                "New Image",
                true,
                2
        );

        when(testedProductRepository.findById(testProductId)).thenReturn(Optional.of(testProduct));
        when(testedImageRepository.findById(newImageId)).thenReturn(Optional.of(newImage));
        when(testedImageMappingRepository.existsById(any(ProductImageMappingId.class))).thenReturn(false);
        when(testedProductRepository.save(any(Product.class))).thenAnswer(invocation -> {
            return invocation.<Product>getArgument(0);
        });
        when(testedImageMapper.mapToProductImageDto(any(ProductImageMapping.class))).thenReturn(expectedDto);

        // when
        ProductImageDto result = testedProductImageService.associateImageWithProduct(testProductId, request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.imageId()).isEqualTo(newImageId);
        assertThat(result.imageUrl()).isEqualTo("https://example.com/new.jpg");
        assertThat(result.altText()).isEqualTo("New Image");
        assertThat(result.isPrimary()).isTrue();
        assertThat(result.displayOrder()).isEqualTo(2);

        verify(testedProductRepository).findById(testProductId);
        verify(testedImageRepository).findById(newImageId);
        verify(testedImageMappingRepository).existsById(any(ProductImageMappingId.class));
        verify(testedProductRepository).save(testProduct);

        assertThat(testProduct.getImageMappings()).isNotEmpty();
        assertThat(testProduct.getImageMappings())
                .anySatisfy(mapping -> {
                    assertThat(mapping.getImage().getImageId()).isEqualTo(newImageId);
                    assertThat(mapping.isPrimary()).isTrue();
                    assertThat(mapping.getDisplayOrder()).isEqualTo(2);
                });

        assertThat(newImage.getProductMappings())
                .anySatisfy(mapping -> {
                    assertThat(mapping.getProduct().getProductId()).isEqualTo(testProductId);
                });

        verify(testedImageMapper).mapToProductImageDto(any(ProductImageMapping.class));
    }

    @Test
    void associateImageWithProduct_WhenPrimary_ClearsOtherPrimaryImages() {
        // given
        Long newImageId = 2L;
        ProductImage newImage = new ProductImage();
        newImage.setImageId(newImageId);
        newImage.setImageUrl("https://example.com/new.jpg");
        newImage.setAltText("New Primary Image");

        ProductImage existingPrimaryImage = new ProductImage();
        existingPrimaryImage.setImageId(1L);
        ProductImageMapping existingPrimaryMapping = new ProductImageMapping(testProduct, existingPrimaryImage, true, 1);
        testProduct.getImageMappings().add(existingPrimaryMapping);

        AssociateImageRequestDto request = new AssociateImageRequestDto(newImageId, true, 2);

        ProductImageDto expectedDto = new ProductImageDto(newImageId, "url", "alt", true, 2);

        when(testedProductRepository.findById(testProductId)).thenReturn(Optional.of(testProduct));
        when(testedImageRepository.findById(newImageId)).thenReturn(Optional.of(newImage));
        when(testedImageMappingRepository.existsById(any(ProductImageMappingId.class))).thenReturn(false);
        when(testedProductRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(testedImageMapper.mapToProductImageDto(any(ProductImageMapping.class))).thenReturn(expectedDto);

        // when
        ProductImageDto result = testedProductImageService.associateImageWithProduct(testProductId, request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.isPrimary()).isTrue();
        assertThat(existingPrimaryMapping.isPrimary()).isFalse();
        long primaryCount = testProduct.getImageMappings().stream()
                .filter(ProductImageMapping::isPrimary)
                .count();
        assertThat(primaryCount).isEqualTo(1);

        verify(testedProductRepository).save(testProduct);
    }

    @Test
    void associateImageWithProduct_WhenImageAlreadyAssociated_ThrowsException() {
        // given
        Long imageId = 2L;
        ProductImage image = new ProductImage();
        image.setImageId(imageId);

        AssociateImageRequestDto request = new AssociateImageRequestDto(imageId, false, 1);

        ProductImageMappingId mappingId = ProductImageMapping.createId(testProductId, imageId);

        when(testedProductRepository.findById(testProductId)).thenReturn(Optional.of(testProduct));
        when(testedImageRepository.findById(imageId)).thenReturn(Optional.of(image));
        when(testedImageMappingRepository.existsById(mappingId)).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> testedProductImageService.associateImageWithProduct(testProductId, request))
                .isInstanceOf(ImageAssociatedException.class)
                .hasMessageContaining("already associated");

        verify(testedProductRepository, never()).save(any());
    }

    @Test
    void associateImageWithProduct_WhenProductNotFound_ThrowsException() {
        // given
        UUID nonExistentProductId = UUID.randomUUID();
        AssociateImageRequestDto request = new AssociateImageRequestDto(1L, false, 1);

        when(testedProductRepository.findById(nonExistentProductId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> testedProductImageService.associateImageWithProduct(nonExistentProductId, request))
                .isInstanceOf(ProductNotFoundException.class);

        verify(testedProductRepository, never()).save(any());
    }

    @Test
    void associateImageWithProduct_WhenImageNotFound_ThrowsException() {
        // given
        Long nonExistentImageId = 999L;
        AssociateImageRequestDto request = new AssociateImageRequestDto(nonExistentImageId, false, 1);

        when(testedProductRepository.findById(testProductId)).thenReturn(Optional.of(testProduct));
        when(testedImageRepository.findById(nonExistentImageId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> testedProductImageService.associateImageWithProduct(testProductId, request))
                .isInstanceOf(ImageNotFoundException.class);

        verify(testedProductRepository, never()).save(any());
    }

}

