package org.peter_lukas.shirtso.product;

import java.math.*;
import java.util.*;

public record ProductDto (
        UUID productId,
        String productName,
        String description,
        BigDecimal price,
        String currency,
        int imageId,
        int categoryId,
        String supplier,
        long stock,
        Sizes size
) {

}
