package org.peter_lukas.shirtso.commercial.product;

import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import org.peter_lukas.shirtso.analytics.LogExecutionTime;
import org.peter_lukas.shirtso.commercial.product.dto.NewProductDto;
import org.peter_lukas.shirtso.commercial.product.dto.ProductDto;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static org.peter_lukas.shirtso.auth.config.SpringSecurityConfig.USER_WRITE;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @LogExecutionTime
    @GetMapping
    public List<ProductDto> getProducts() {
        return productService.getAllProducts();
    }

    @GetMapping(params = {"page", "size"})
    public List<ProductDto>getProducts(Pageable pageable) {
        return productService.getAllProductsPage(pageable);
    }

    @RolesAllowed(USER_WRITE)
    @PostMapping
    public ProductDto addNewProduct(@Valid @RequestBody NewProductDto newProduct) {
        return productService.addNewProduct(newProduct);
    }

    @GetMapping(params = {"subcategoryId"})
    public List<ProductDto> getProductsBySubcategoryId(@RequestParam int subcategoryId) {
        return productService.getProductsBySubcategoryId(subcategoryId);
    }

    @GetMapping(params = {"size"})
    public List<ProductDto>getProductsBySize(@RequestParam Sizes size) {
        return productService.getProductsBySize(size);
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

    @GetMapping("/sizes")
    public List<Sizes> getSizes() {
        return List.of(Sizes.values());
    }

    @GetMapping(params = {"size", "categoryId"})
    public List<ProductDto> getProductsBySizeAndCategoryId(@RequestParam Sizes size, int categoryId) {
        return productService.getProductsBySizeAndCategoryId(size, categoryId);
    }

    @GetMapping(params = {"size", "subcategoryId"})
    public List<ProductDto> getProductsBySizeAndSubcategory(@RequestParam Sizes size, int subcategoryId) {
        return productService.getProductsBySizeAndSubcategoryId(size, subcategoryId);
    }

    @GetMapping("/{productId}")
    public ProductDto getProductById(@PathVariable UUID productId) {
        return productService.getProductById(productId);
    }

    @GetMapping(params = {"categoryId"})
    public List<ProductDto> getProductsByCategoryId(@RequestParam int categoryId) {
        return productService.getProductsByCategoryId(categoryId);
    }
}
