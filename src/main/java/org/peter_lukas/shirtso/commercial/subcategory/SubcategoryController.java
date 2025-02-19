package org.peter_lukas.shirtso.commercial.subcategory;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subcategories")
public class SubcategoryController {

    private final SubcategoryService subcategoryService;

    public SubcategoryController(SubcategoryService subcategoryService) {
        this.subcategoryService = subcategoryService;
    }

    @GetMapping("/{categoryId}")
    public List<SubcategoryDto> getSubcategoriesByCategoryId(@PathVariable int categoryId) {
        return subcategoryService.getAllSubcategoriesByCategoryId(categoryId);
    }
}
//TODO - nie działa autoryzacja