package org.peter_lukas.shirtso.commercial.category;

import org.peter_lukas.shirtso.commercial.subcategory.SubcategoryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryService(CategoryRepository categoryRepository, CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    public List<CategoryDto> getAllCategories() {
        return categoryRepository.findAllBy().stream()
                .map(categoryMapper::mapCategoryEntityToDto)
                .toList();
    }

    public List<CategoryDto> getAllCategoriesAndSubcategories() {
        return categoryRepository.findAllWithSubcategories().stream()
                .map(categoryMapper::mapCategoryEntityToDto)
                .toList();
    }
}
