package org.peter_lukas.shirtso.commercial.product;

import org.peter_lukas.shirtso.messages.Alerts;
import org.peter_lukas.shirtso.commercial.product.validation.ProductDuplicationException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductService(ProductRepository productRepository, ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    public List<ProductDto> getAllProducts() {
        return productRepository.findAllBy().stream()
                .map(productMapper::mapProductEntityToDto)
                .toList();
    }

    public List<ProductDto> getAllProductsPage(Pageable pageable) {
        return productRepository.findAllBy(pageable).stream()
                .map(productMapper::mapProductEntityToDto)
                .toList();
    }

    public ProductDto addNewProduct(NewProductDto newProduct) {
        boolean exists = productRepository.existsByAttributes(
                newProduct.productName(),
                newProduct.description(),
                newProduct.price(),
                newProduct.currency(),
                newProduct.imageId(),
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

    public List<ProductDto> getProductsBySubcategoryId(int subcategoryId) {
        return productRepository.findAllBySubcategoryId(subcategoryId).stream()
                .map(productMapper::mapProductEntityToDto)
                .toList();
    }

    public List<ProductDto> getProductsBySize(Sizes size) {
        return productRepository.findAllBySize(size).stream()
                .map(productMapper::mapProductEntityToDto)
                .toList();
    }

    public List<ProductDto> getProductsInStock() {
        return productRepository.findAllInStock().stream()
                .map(productMapper::mapProductEntityToDto)
                .toList();
    }

    public List<ProductDto> getProductsNotInStock() {
        return productRepository.findAllZeroStock().stream()
                .map(productMapper::mapProductEntityToDto)
                .toList();
    }

    public List<ProductDto> getProductsTopUpStock() {
        return productRepository.findAllLessThan3Stock().stream()
                .map(productMapper::mapProductEntityToDto)
                .toList();
    }

    public List<ProductDto> getProductsByProductName(String productName) {
        return productRepository.findAllByProductName(productName).stream()
                .map(productMapper::mapProductEntityToDto)
                .toList();
    }
}
