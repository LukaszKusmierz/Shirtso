package org.peter_lukas.shirtso.commercial.product;

import jakarta.validation.Valid;
import org.peter_lukas.shirtso.commercial.product.dto.NewProductDto;
import org.peter_lukas.shirtso.commercial.product.dto.ProductDto;
import org.peter_lukas.shirtso.commercial.product.dto.ProductVariantDto;
import org.peter_lukas.shirtso.commercial.product.dto.UpdateProductDto;
import org.peter_lukas.shirtso.commercial.product.image.ProductImageMapping;
import org.peter_lukas.shirtso.commercial.product.image.dto.ProductImageDto;
import org.peter_lukas.shirtso.commercial.product.validation.ProductNotFoundException;
import org.peter_lukas.shirtso.messages.Alerts;
import org.peter_lukas.shirtso.commercial.product.validation.ProductDuplicationException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductService(ProductRepository productRepository, ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    @Transactional(readOnly = true)
    public List<ProductDto> getAllProducts() {
        return productRepository.findAllWithImages().stream()
                .map(productMapper::mapProductEntityToDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductDto> getAllProductsPage(Pageable pageable) {
        return productRepository.findAllWithImages(pageable).stream()
                .map(productMapper::mapProductEntityToDto)
                .toList();
    }

    @Transactional
    public ProductDto addNewProduct(NewProductDto newProduct) {
        boolean exists = productRepository.existsByAttributes(
                newProduct.productName(),
                newProduct.description(),
                newProduct.price(),
                newProduct.currency(),
                newProduct.subcategoryId(),
                newProduct.supplier(),
                newProduct.stock(),
                newProduct.size()
        );

        if (exists) {
            throw new ProductDuplicationException(Alerts.DUPLICATE_PRODUCT);
        }
        Product addedProduct = productRepository.save(productMapper.mapNewProductDtoToEntity(newProduct));
        return productMapper.mapProductEntityToDto(addedProduct);
    }

    @Transactional(readOnly = true)
    public List<ProductDto> getProductsBySubcategoryId(int subcategoryId) {
        return productRepository.findAllBySubcategoryIdWithImages(subcategoryId).stream()
                .map(productMapper::mapProductEntityToDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductDto> getProductsBySize(Sizes size) {
        return productRepository.findAllBySizeWithImages(size).stream()
                .map(productMapper::mapProductEntityToDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductDto> getProductsInStock() {
        return productRepository.findAllInStockWithImages().stream()
                .map(productMapper::mapProductEntityToDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductDto> getProductsNotInStock() {
        return productRepository.findAllZeroStockWithImages().stream()
                .map(productMapper::mapProductEntityToDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductDto> getProductsTopUpStock() {
        return productRepository.findAllLessThan3StockWithImages().stream()
                .map(productMapper::mapProductEntityToDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductDto> getProductsByProductName(String productName) {
        return productRepository.findAllByProductNameWithImages(productName).stream()
                .map(productMapper::mapProductEntityToDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductDto> getProductsBySizeAndCategoryId(Sizes size, int categoryId) {
        return productRepository.findProductsBySizeAndCategoryIdWithImages(size, categoryId).stream()
                .map(productMapper::mapProductEntityToDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductDto> getProductsBySizeAndSubcategoryId(Sizes size, int subcategoryId) {
        return productRepository.findProductsBySizeAndSubcategoryIdWithImages(size, subcategoryId).stream()
                .map(productMapper::mapProductEntityToDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductDto getProductById(UUID productId) {
        return productRepository.getProductByProductIdWithImages(productId)
                .map(productMapper::mapProductEntityToDto)
                .orElseThrow(() -> new ProductNotFoundException(Alerts.PRODUCT_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public List<ProductDto> getProductsByCategoryId(int categoryId) {
        return productRepository.getProductsByCategoryIdWithImages(categoryId).stream()
                .map(productMapper::mapProductEntityToDto)
                .toList();
    }

    @Transactional
    public ProductDto updateProduct(UUID productId, @Valid UpdateProductDto updateProduct) {
        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(Alerts.PRODUCT_NOT_FOUND));

        existingProduct.setProductName(updateProduct.productName());
        existingProduct.setDescription(updateProduct.description());
        existingProduct.setPrice(updateProduct.price());
        existingProduct.setCurrency(updateProduct.currency());
        existingProduct.setSubcategoryId(updateProduct.subcategoryId());
        existingProduct.setSupplier(updateProduct.supplier());
        existingProduct.setStock(updateProduct.stock());
        existingProduct.setSize(updateProduct.size());

        Product updatedProduct = productRepository.save(existingProduct);
        return productMapper.mapProductEntityToDto(updatedProduct);
    }

    @Transactional(readOnly = true)
    public List<ProductVariantDto> getAllProductsGrouped() {
        List<Product> products = productRepository.findAllWithImages();
        return groupProductsByVariant(products);
    }

    @Transactional(readOnly = true)
    public List<ProductVariantDto> getProductsGroupedBySubcategory(int subcategoryId) {
        List<Product> products = productRepository.findAllBySubcategoryIdWithImages(subcategoryId);
        return groupProductsByVariant(products);
    }

    @Transactional(readOnly = true)
    public List<ProductVariantDto> getProductsGroupedByCategory(int categoryId) {
        List<Product> products = productRepository.getProductsByCategoryIdWithImages(categoryId);
        return groupProductsByVariant(products);
    }

    @Transactional(readOnly = true)
    public List<ProductVariantDto> getProductsGroupedInStock() {
        List<Product> products = productRepository.findAllInStockWithImages();
        return groupProductsByVariant(products);
    }

    @Transactional(readOnly = true)
    public List<ProductVariantDto> getProductsGroupedByName(String productName) {
        List<Product> products = productRepository.findAllByProductNameWithImages(productName);
        return groupProductsByVariant(products);
    }

    @Transactional(readOnly = true)
    public ProductVariantDto getProductVariantByName(String productName, String description) {
        List<Product> products = productRepository.findAllByProductNameWithImages(productName);

        List<Product> matchingProducts = products.stream()
                .filter(p -> p.getDescription().equals(description))
                .toList();

        if (matchingProducts.isEmpty()) {
            throw new ProductNotFoundException(Alerts.PRODUCT_NOT_FOUND);
        }

        List<ProductVariantDto> grouped = groupProductsByVariant(matchingProducts);
        return grouped.isEmpty() ? null : grouped.get(0);
    }

    private List<ProductVariantDto> groupProductsByVariant(List<Product> products) {
        Map<String, List<Product>> groupedProducts = products.stream()
                .collect(Collectors.groupingBy(p ->
                        p.getProductName() + "|" +
                                p.getDescription() + "|" +
                                p.getPrice() + "|" +
                                p.getCurrency() + "|" +
                                p.getSupplier() + "|" +
                                p.getSubcategoryId()
                ));

        return groupedProducts.values().stream()
                .map(this::createProductVariantDto)
                .toList();
    }

    private ProductVariantDto createProductVariantDto(List<Product> variants) {
        Product representative = variants.get(0);

        List<ProductVariantDto.SizeVariant> sizeVariants = variants.stream()
                .map(p -> new ProductVariantDto.SizeVariant(
                        p.getProductId(),
                        p.getSize(),
                        p.getStock()
                ))
                .sorted(Comparator.comparing(sv -> sv.size().ordinal()))
                .toList();

        long totalStock = variants.stream()
                .mapToLong(Product::getStock)
                .sum();

        List<ProductImageDto> images = representative.getImageMappings().stream()
                .sorted(Comparator.comparing(ProductImageMapping::getDisplayOrder))
                .map(mapping -> new ProductImageDto(
                        mapping.getImage().getImageId(),
                        mapping.getImage().getImageUrl(),
                        mapping.getImage().getAltText(),
                        mapping.isPrimary(),
                        mapping.getDisplayOrder()
                ))
                .toList();

        return new ProductVariantDto(
                representative.getProductName(),
                representative.getDescription(),
                representative.getPrice(),
                representative.getCurrency(),
                representative.getSubcategoryId(),
                representative.getSupplier(),
                sizeVariants,
                images,
                totalStock
        );
    }
}
