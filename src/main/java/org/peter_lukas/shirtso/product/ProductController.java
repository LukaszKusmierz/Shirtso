package org.peter_lukas.shirtso.product;

import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import org.peter_lukas.shirtso.analytics.LogExecutionTime;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.peter_lukas.shirtso.auth.config.SpringSecurityConfig.DEVELOPER_READ;
import static org.peter_lukas.shirtso.auth.config.SpringSecurityConfig.DEVELOPER_WRITE;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @RolesAllowed(DEVELOPER_READ)
    @LogExecutionTime
    @GetMapping
    public List<ProductDto> getProducts() {
        return productService.getAllProducts();
    }

    @RolesAllowed(DEVELOPER_READ)
    @GetMapping(params = {"page", "size"})
    public List<ProductDto>getProducts(Pageable pageable) {
        return productService.getAllProductsPage(pageable);
    }

    @RolesAllowed(DEVELOPER_WRITE)
    @PostMapping
    public ProductDto addNewProduct(@Valid @RequestBody NewProductDto newProduct) {
        return productService.addNewProduct(newProduct);
    }

    @RolesAllowed(DEVELOPER_READ)
    @GetMapping(params = {"categoryId"})
    public List<ProductDto> getProductsByCategoryId(@RequestParam int categoryId) {
        return productService.getProductsByCategoryId(categoryId);
    }

    @RolesAllowed(DEVELOPER_READ)
    @GetMapping(params = {"size"})
    public List<ProductDto>getProductsBySize(@RequestParam Sizes size) {
        return productService.getProductsBySize(size);
    }

    @RolesAllowed(DEVELOPER_READ)
    @GetMapping("/in-stock")
    public List<ProductDto> getProductsInStock() { return productService.getProductsInStock();
    }

    @RolesAllowed(DEVELOPER_READ)
    @GetMapping("/not-in-stock")
    public List<ProductDto> getProductsNotInStock() { return productService.getProductsNotInStock();}

    @RolesAllowed(DEVELOPER_READ)
    @GetMapping("/top-up-stock")
    public List<ProductDto> getProductsTopUpStock() {
        return productService.getProductsTopUpStock();
    }

    @RolesAllowed(DEVELOPER_READ)
    @GetMapping(params = {"productName"})
    public List<ProductDto> getProductsByProductName(@RequestParam String productName) {
        return productService.getProductsByProductName(productName);
    }
}
