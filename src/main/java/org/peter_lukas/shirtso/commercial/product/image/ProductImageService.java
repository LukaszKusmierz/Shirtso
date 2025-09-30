package org.peter_lukas.shirtso.commercial.product.image;

import jakarta.validation.constraints.NotNull;
import org.peter_lukas.shirtso.commercial.product.Product;
import org.peter_lukas.shirtso.commercial.product.ProductRepository;
import org.peter_lukas.shirtso.commercial.product.image.dto.AssociateImageRequestDto;
import org.peter_lukas.shirtso.commercial.product.image.dto.CreateImageRequestDto;
import org.peter_lukas.shirtso.commercial.product.image.dto.ProductImageDto;
import org.peter_lukas.shirtso.commercial.product.validation.*;
import org.peter_lukas.shirtso.messages.Alerts;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProductImageService {
    private final ProductRepository productRepository;
    private final ProductImageRepository imageRepository;
    private final ProductImageMappingRepository imageMappingRepository;
    private final ProductImageMapper imageMapper;

    public ProductImageService(ProductRepository productRepository,
                               ProductImageRepository imageRepository,
                               ProductImageMappingRepository imageMappingRepository,
                               ProductImageMapper imageMapper) {
        this.productRepository = productRepository;
        this.imageRepository = imageRepository;
        this.imageMappingRepository = imageMappingRepository;
        this.imageMapper = imageMapper;
    }

    public List<ProductImageDto> getProductImages(UUID productId) {
        return imageMappingRepository.findByProduct_ProductIdOrderByDisplayOrderAsc(productId).stream()
                .map(imageMapper::mapToProductImageDto)
                .toList();
    }

    public Optional<ProductImageDto> getProductPrimaryImage(UUID productId) {
        return imageMappingRepository.findByProduct_ProductIdAndIsPrimaryTrue(productId)
                .map(imageMapper::mapToProductImageDto);
    }

    @Transactional
    public ProductImageDto associateImageWithProduct(UUID productId, AssociateImageRequestDto request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(Alerts.PRODUCT_NOT_FOUND));
        ProductImage image = imageRepository.findById(request.imageId())
                .orElseThrow(() -> new ImageNotFoundException(Alerts.IMAGE_NOT_FOUND));
        ProductImageMappingId mappingId = ProductImageMapping.createId(productId, request.imageId());
        if (imageMappingRepository.existsById(mappingId)) {
            throw new ImageAssociatedException(Alerts.IMAGE_ALREADY_ASSOCIATED);
        }

        ProductImageMapping mapping = new ProductImageMapping(product, image, request.isPrimary(), request.displayOrder()
        );

        if (request.isPrimary()) {
            clearCurrentPrimaryImage(productId);
        }
        ProductImageMapping savedMapping = imageMappingRepository.save(mapping);
        return imageMapper.mapToProductImageDto(savedMapping);
    }

    @Transactional
    public void updatePrimaryImageStatus(UUID productId, @NotNull(message = "Image ID can not be null") Long newPrimaryImageId) {
        ProductImageMappingId mappingId = ProductImageMapping.createId(productId, newPrimaryImageId);
        ProductImageMapping newPrimaryMapping = imageMappingRepository.findById(mappingId)
                .orElseThrow(() -> new ImageNotAssociatedException(Alerts.IMAGE_ASSOCIATION_NOT_FOUND));

        clearCurrentPrimaryImage(productId);

        newPrimaryMapping.setPrimary(true);
        imageMappingRepository.save(newPrimaryMapping);
    }

    @Transactional
    public void removeImageFromProduct(UUID productId, Long imageId) {
        ProductImageMappingId mappingId = ProductImageMapping.createId(productId, imageId);
        imageMappingRepository.findById(mappingId)
                .ifPresent(mapping -> {
                    boolean wasPrimary = mapping.isPrimary();
                    imageMappingRepository.delete(mapping);
                    if (wasPrimary) {
                        List<ProductImageMapping> remainingImages =
                                imageMappingRepository.findByProduct_ProductIdOrderByDisplayOrderAsc(productId);
                        if (!remainingImages.isEmpty()) {
                            ProductImageMapping newPrimary = remainingImages.get(0);
                            newPrimary.setPrimary(true);
                            imageMappingRepository.save(newPrimary);
                        }
                    }
                });
    }

    @Transactional
    public ProductImage createImage(CreateImageRequestDto request) {
        ProductImage image = new ProductImage();
        image.setImageUrl(request.imageUrl());
        image.setAltText(request.altText());
        return imageRepository.save(image);
    }

    @Transactional
    public ProductImage updateImage(Long imageId, CreateImageRequestDto request) {
        ProductImage image = imageRepository.findById(imageId)
                .orElseThrow(() -> new ImageNotFoundException(Alerts.IMAGE_NOT_FOUND));
        image.setImageUrl(request.imageUrl());
        image.setAltText(request.altText());
        return imageRepository.save(image);
    }

    @Transactional
    public void deleteImage(Long imageId) {
        ProductImage image = imageRepository.findById(imageId)
                .orElseThrow(() -> new ImageNotFoundException(Alerts.IMAGE_NOT_FOUND));
        if (!image.getProductMappings().isEmpty()) {
            throw new ImageInUseException(Alerts.IMAGE_IN_USE);
        }
        imageRepository.delete(image);
    }

    public List<UUID> getProductsUsingImage(Long imageId) {
        return imageRepository.findById(imageId)
                .map(image -> image.getProductMappings().stream()
                        .map(mapping -> mapping.getProduct().getProductId())
                        .toList())
                .orElse(List.of());
    }

    private void clearCurrentPrimaryImage(UUID productId) {
        imageMappingRepository.findByProduct_ProductIdAndIsPrimaryTrue(productId)
                .ifPresent(mapping -> {
                    mapping.setPrimary(false);
                    imageMappingRepository.save(mapping);
                });
    }

    @Transactional
    public void bulkRemoveImagesFromProduct(UUID productId, List<Long> imageIds) {
        List<ProductImageMappingId> mappingIds = imageIds.stream()
                .map(imageId -> ProductImageMapping.createId(productId, imageId))
                .toList();

        List<ProductImageMapping> mappingsToDelete = imageMappingRepository.findAllById(mappingIds);

        boolean removingPrimary = mappingsToDelete.stream().anyMatch(ProductImageMapping::isPrimary);

        imageMappingRepository.deleteAll(mappingsToDelete);

        if (removingPrimary) {
            List<ProductImageMapping> remainingImages =
                    imageMappingRepository.findByProduct_ProductIdOrderByDisplayOrderAsc(productId);
            if (!remainingImages.isEmpty()) {
                ProductImageMapping newPrimary = remainingImages.get(0);
                newPrimary.setPrimary(true);
                imageMappingRepository.save(newPrimary);
            }
        }
    }

    public Optional<ProductImage> getImageById(Long imageId) {
        return imageRepository.findById(imageId);
    }

    public List<ProductImage> getAllImages() {
        return imageRepository.findAll();
    }
}
