package org.peter_lukas.shirtso.commercial.subcategory;

import org.peter_lukas.shirtso.commercial.category.Category;
import org.springframework.stereotype.Component;

@Component
public class SubcategoryMapper {
    public SubcategoryDto mapSubcategoryEntityToDto(Subcategory entity) {
        return new SubcategoryDto(
                entity.getSubcategoryId(),
                entity.getSubcategoryName(),
                entity.getCategory().getCategoryId()
        );
    }

    public Subcategory mapNewSubcategoryDtoToEntity(SubcategoryDto dto) {
        Category category = categoryRepository.findById(dto.subcategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found for id: " + dto.categoryId()));

        return new Subcategory(
                dto.subcategoryName(),
                category
        );
    }
}
