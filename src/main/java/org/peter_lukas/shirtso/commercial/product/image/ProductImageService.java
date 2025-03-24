package org.peter_lukas.shirtso.commercial.product.image;

import org.peter_lukas.shirtso.commercial.product.Product;
import org.peter_lukas.shirtso.commercial.product.ProductRepository;
import org.peter_lukas.shirtso.commercial.product.validation.ImageNotFoundException;
import org.peter_lukas.shirtso.commercial.product.validation.ProductNotFoundException;
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
    public ProductImageDto associateImageWithProduct(UUID productId, AssociateImageRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(Alerts.PRODUCT_NOT_FOUND));

        ProductImage image = imageRepository.findById(request.imageId())
                .orElseThrow(() -> new ImageNotFoundException(Alerts.IMAGE_NOT_FOUND));

        ProductImageMapping mapping = new ProductImageMapping(
                product,
                image,
                request.isPrimary(),
                request.displayOrder()
        );

        if (request.isPrimary()) {
            updatePrimaryImageStatus(productId, request.imageId());
        }

        ProductImageMapping savedMapping = imageMappingRepository.save(mapping);
        return imageMapper.mapToProductImageDto(savedMapping);
    }

    @Transactional
    public void updatePrimaryImageStatus(UUID productId, long newPrimaryImageId) {
        imageMappingRepository.findByProduct_ProductIdAndIsPrimaryTrue(productId)
                .ifPresent(imageMappingRepository::save);

        ProductImageMappingId mappingId = new ProductImageMappingId(productId, newPrimaryImageId);
        imageMappingRepository.findById(mappingId)
                .ifPresent(imageMappingRepository::save);
    }

    @Transactional
    public void removeImageFromProduct(UUID productId, long imageId) {
        ProductImageMappingId mappingId = new ProductImageMappingId(productId, imageId);
        imageMappingRepository.findById(mappingId)
                .ifPresent(imageMappingRepository::delete);
    }

    @Transactional
    public ProductImage createImage(CreateImageRequest request) {
        ProductImage image = new ProductImage();
        image.setImageUrl(request.imageUrl());
        image.setAltText(request.altText());
        return imageRepository.save(image);
    }
}
