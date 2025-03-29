package org.peter_lukas.shirtso.commercial.category;

import org.peter_lukas.shirtso.commercial.category.dto.CategoryDto;
import org.peter_lukas.shirtso.commercial.category.dto.NewCategoryDto;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {
    public CategoryDto mapCategoryEntityToDto(Category entity) {
        return new CategoryDto(
                entity.getCategoryId(),
                entity.getCategoryName()
        );
    }

    public Category mapNewCategoryDtoToEntity(NewCategoryDto dto) {
        return new Category(dto.categoryName());
    }
}
