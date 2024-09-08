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
        return productService.getAllProductsPage();
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
    public List<ProductDto> getProducts(@RequestParam int categoryId) {
        return productService.getProductByCategoryId(categoryId);
    }
}
