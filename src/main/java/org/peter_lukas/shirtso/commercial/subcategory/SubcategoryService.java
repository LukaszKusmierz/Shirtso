package org.peter_lukas.shirtso.commercial.subcategory;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubcategoryService {
    private final SubcategoryRepository subcategoryRepository;
    private final SubcategoryMapper subcategoryMapper;

    public SubcategoryService(SubcategoryRepository subcategoryRepository, SubcategoryMapper subcategoryMapper) {
        this.subcategoryRepository = subcategoryRepository;
        this.subcategoryMapper = subcategoryMapper;
    }

    public List<Subcategory> getAllSubcategoriesByCategoryId() {
    }
}
