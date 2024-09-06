package org.peter_lukas.shirtso;

import java.math.*;
import java.util.*;

public record ProductDto (
        UUID id,
        String productName,
        String description,
        BigDecimal price,
        String currency,
        int imageId,
        int category,
        String supplier,
        long quantity,
        Sizes size
) {

}
