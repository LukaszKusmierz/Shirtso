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

    @Transactional
    public List<ProductImageDto> getProductImages(UUID productId) {
        return imageMappingRepository.findByProduct_ProductIdOrderByDisplayOrderAsc(productId).stream()
                .map(imageMapper::mapToProductImageDto)
                .toList();
    }

    @Transactional
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

        if (request.isPrimary()) {
            clearCurrentPrimaryImage(productId);
        }

        product.addImage(image, request.isPrimary(), request.displayOrder());
        Product savedProduct = productRepository.save(product);

        ProductImageMapping savedMapping = savedProduct.getImageMappings().stream()
                .filter(m -> m.getImage().getImageId().equals(request.imageId()))
                .findFirst()
                .orElseThrow(() -> new CreateImageMappinException(Alerts.IMAGE_MAPPING_CREATE_FAILED));

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
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(Alerts.PRODUCT_NOT_FOUND));

        ProductImageMappingId mappingId = ProductImageMapping.createId(productId, imageId);
        imageMappingRepository.findById(mappingId)
                .ifPresent(mapping -> {
                    boolean wasPrimary = mapping.isPrimary();

                    product.getImageMappings().remove(mapping);
                    mapping.getImage().getProductMappings().remove(mapping);

                    imageMappingRepository.delete(mapping);

                    if (wasPrimary && !product.getImageMappings().isEmpty()) {
                        ProductImageMapping newPrimary = product.getImageMappings().stream()
                                .min((m1, m2) -> Integer.compare(m1.getDisplayOrder(), m2.getDisplayOrder()))
                                .orElseThrow();
                        newPrimary.setPrimary(true);
                        imageMappingRepository.save(newPrimary);
                    }
                });
    }

    @Transactional
    public ProductImage createImageIfNotExists(CreateImageRequestDto request) {
        return imageRepository.findByImageUrl(request.imageUrl())
                .orElseGet(() -> {
                    ProductImage image = new ProductImage();
                    image.setImageUrl(request.imageUrl());
                    image.setAltText(request.altText());
                    return imageRepository.save(image);
                });
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
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(Alerts.PRODUCT_NOT_FOUND));

        List<ProductImageMappingId> mappingIds = imageIds.stream()
                .map(imageId -> ProductImageMapping.createId(productId, imageId))
                .toList();

        List<ProductImageMapping> mappingsToDelete = imageMappingRepository.findAllById(mappingIds);
        boolean removingPrimary = mappingsToDelete.stream().anyMatch(ProductImageMapping::isPrimary);

        mappingsToDelete.forEach(mapping -> {
            product.getImageMappings().remove(mapping);
            mapping.getImage().getProductMappings().remove(mapping);
        });

        imageMappingRepository.deleteAll(mappingsToDelete);

        if (removingPrimary && !product.getImageMappings().isEmpty()) {
            ProductImageMapping newPrimary = product.getImageMappings().stream()
                    .min((m1, m2) -> Integer.compare(m1.getDisplayOrder(), m2.getDisplayOrder()))
                    .orElseThrow();
            newPrimary.setPrimary(true);
            imageMappingRepository.save(newPrimary);
        }
    }

    public Optional<ProductImage> getImageById(Long imageId) {
        return imageRepository.findById(imageId);
    }

    public List<ProductImage> getAllImages() {
        return imageRepository.findAll();
    }
}
