package org.peter_lukas.shirtso.commercial.subcategory;

import org.peter_lukas.shirtso.commercial.category.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubcategoryRepository extends JpaRepository<Subcategory, Integer> {

    List<Subcategory> findByCategory(Category category);

    List<Subcategory> findAllByCategory_CategoryId(Integer categoryId);

    Optional<Subcategory> findBySubcategoryName(String subcategoryName);

    List<Subcategory> findBySubcategoryNameContainingIgnoreCase(String subcategoryNamePart);
}
