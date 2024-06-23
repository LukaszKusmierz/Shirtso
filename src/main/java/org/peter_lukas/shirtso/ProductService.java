package org.peter_lukas.shirtso;

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

    public List<ProductDto> getAllProducts(Pageable pageable) {
        return productRepository.findAllByOrderByProductNameAsc(pageable).stream()
                .map(productMapper::mapProductEntityToDto)
                .toList();
    }

    public ProductDto addNewProduct(NewProductDto newProduct) {
        Product addedProduct = productRepository.save(productMapper.mapNewProductDtoToEntity(newProduct));
        return productMapper.mapProductEntityToDto(addedProduct);
    }
}
