package org.peter_lukas.shirtso.product;

import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public List<ProductDto> getProducts() {
        return productService.getAllProducts();
    }

    @GetMapping(params = {"page", "size"})
    public List<ProductDto>getProducts(Pageable pageable) {
        return productService.getAllProductsPage(pageable);
    }

    @PostMapping
    public ProductDto addNewProduct(@RequestBody NewProductDto newProduct) {
        return productService.addNewProduct(newProduct);
    }

    @GetMapping(params = {"categoryId"})
    public List<ProductDto> getProductsByCategoryId(@RequestParam int categoryId) {
        return productService.getProductByCategoryId(categoryId);
    }

    @GetMapping(params = {"size"})
    public List<ProductDto>getProductsBySize(@RequestParam Sizes size) {
        return productService.getProductBySize(size);
    }

    @GetMapping("/in-stock")
    public List<ProductDto> getProductsInStock() { return productService.getProductsInStock();
    }

    @GetMapping("/not-in-stock")
    public List<ProductDto> getProductsNotInStock() { return productService.getProductsNotInStock();}

    @GetMapping("/top-up-stock")
    public List<ProductDto> getProductsTopUpStock() {
        return productService.getProductsTopUpStock();
    }

    @GetMapping(params = {"productName"})
    public List<ProductDto> getProductsByProductName(@RequestParam String productName) {
        return productService.getProductsByProductName(productName);
    }
}
