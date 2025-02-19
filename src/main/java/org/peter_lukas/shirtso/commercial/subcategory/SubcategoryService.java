package org.peter_lukas.shirtso.commercial.subcategory;

import org.peter_lukas.shirtso.commercial.category.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubcategoryService {
    private final SubcategoryRepository subcategoryRepository;
    private final CategoryRepository categoryRepository;
    private final SubcategoryMapper subcategoryMapper;

    public SubcategoryService(SubcategoryRepository subcategoryRepository, SubcategoryMapper subcategoryMapper,
                              CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
        this.subcategoryRepository = subcategoryRepository;
        this.subcategoryMapper = subcategoryMapper;
    }

    public List<SubcategoryDto> getAllSubcategoriesByCategoryId(int categoryId) {
        return subcategoryRepository.findAllByCategory_CategoryId(categoryId).stream()
                .map(subcategoryMapper::mapSubcategoryEntityToDto)
                .toList();

    }
}
