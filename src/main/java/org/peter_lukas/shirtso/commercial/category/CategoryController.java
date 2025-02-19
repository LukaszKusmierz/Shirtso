package org.peter_lukas.shirtso.commercial.category;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {this.categoryService = categoryService; }

    @GetMapping
    public List<CategoryDto> getCategories() { return categoryService.getAllCategories();}

    @GetMapping("/subcategories")
    public List<CategoryDto> getAllCategoriesWithSubcategories() {
        return categoryService.getAllCategoriesAndSubcategories();
    }
}
