package org.peter_lukas.shirtso.commercial.product;

import java.math.*;
import java.util.*;

public record ProductDto (
        UUID productId,
        String productName,
        String description,
        BigDecimal price,
        Currencies currency,
        int imageId,
        int subcategoryId,
        String supplier,
        long stock,
        Sizes size
) {

}
