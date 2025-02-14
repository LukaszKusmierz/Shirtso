package org.peter_lukas.shirtso.commercial.subcategory;

import org.peter_lukas.shirtso.commercial.category.Category;
import org.peter_lukas.shirtso.commercial.category.CategoryRepository;
import org.springframework.stereotype.Component;

@Component
public class SubcategoryMapper {

    private final CategoryRepository categoryRepository;

    public SubcategoryMapper(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public SubcategoryDto mapSubcategoryEntityToDto(Subcategory entity) {
        return new SubcategoryDto(
                entity.getSubcategoryId(),
                entity.getSubcategoryName(),
                entity.getCategory().getCategoryId()
        );
    }

    public Subcategory mapNewSubcategoryDtoToEntity(NewSubcategoryDto dto) {
        Category category = categoryRepository.findById(dto.categoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found for id: " + dto.categoryId()));

        return new Subcategory(
                dto.subcategoryName(),
                category
        );
    }
}
