package org.peter_lukas.shirtso.commercial.product.dto;

import org.peter_lukas.shirtso.commercial.product.Currencies;
import org.peter_lukas.shirtso.commercial.product.Sizes;
import org.peter_lukas.shirtso.commercial.product.image.dto.ProductImageDto;

import java.math.*;
import java.util.*;

public record ProductDto (
        UUID productId,
        String productName,
        String description,
        BigDecimal price,
        Currencies currency,
        int subcategoryId,
        String supplier,
        long stock,
        Sizes size,
        List<ProductImageDto> images
) {
}
