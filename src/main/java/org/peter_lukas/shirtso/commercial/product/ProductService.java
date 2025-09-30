package org.peter_lukas.shirtso.commercial.product;

import org.peter_lukas.shirtso.commercial.product.dto.NewProductDto;
import org.peter_lukas.shirtso.commercial.product.dto.ProductDto;
import org.peter_lukas.shirtso.commercial.product.validation.ProductNotFoundException;
import org.peter_lukas.shirtso.messages.Alerts;
import org.peter_lukas.shirtso.commercial.product.validation.ProductDuplicationException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

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
}
