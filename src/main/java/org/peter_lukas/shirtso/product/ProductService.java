package org.peter_lukas.shirtso.product;

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
        return productRepository.findAllByOrderByProductNameAsc().stream()
                .map(productMapper::mapProductEntityToDto)
                .toList();
    }

    public List<ProductDto> getAllProductsPage(Pageable pageable) {
        return productRepository.findAllByOrderByProductNameAsc(pageable).stream()
                .map(productMapper::mapProductEntityToDto)
                .toList();
    }

    public ProductDto addNewProduct(NewProductDto newProduct) {
        Product addedProduct = productRepository.save(productMapper.mapNewProductDtoToEntity(newProduct));
        return productMapper.mapProductEntityToDto(addedProduct);
    }

    public List<ProductDto> getProductByCategoryId(int categoryId) {
        return productRepository.findAllByCategoryId(categoryId).stream()
                .map(productMapper::mapProductEntityToDto)
                .toList();
    }

    public List<ProductDto> getProductBySize(Sizes size) {
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
}
