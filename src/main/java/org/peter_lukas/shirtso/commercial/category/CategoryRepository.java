package org.peter_lukas.shirtso.commercial.category;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {

    List<Category> findAllBy();

    List<Category> findAllByOrderByCategoryNameAsc();

    List<Category> findByCategoryNameContainingIgnoreCase(String categoryNamePart);

    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.subcategories")
    List<Category> findAllWithSubcategories();

    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.subcategories WHERE c.categoryId = ?1")
    Optional<Category> findByCategoryIdWithSubcategories(Integer categoryId);
}
