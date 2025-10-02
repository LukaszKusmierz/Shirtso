package org.peter_lukas.shirtso.commercial.product.dto;

import org.peter_lukas.shirtso.commercial.product.Currencies;
import org.peter_lukas.shirtso.commercial.product.Sizes;
import org.peter_lukas.shirtso.commercial.product.image.dto.ProductImageDto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ProductVariantDto(
        String productName,
        String description,
        BigDecimal price,
        Currencies currency,
        int subcategoryId,
        String supplier,
        List<SizeVariant> sizeVariants,
        List<ProductImageDto> images,
        long totalStock
) {
    public record SizeVariant(
            UUID productId,
            Sizes size,
            long stock
    ) {}

    public List<Sizes> getAvailableSizes() {
        return sizeVariants.stream()
                .map(SizeVariant::size)
                .toList();
    }

    public long getStockForSize(Sizes size) {
        return sizeVariants.stream()
                .filter(v -> v.size() == size)
                .findFirst()
                .map(SizeVariant::stock)
                .orElse(0L);
    }

    public UUID getProductIdForSize(Sizes size) {
        return sizeVariants.stream()
                .filter(v -> v.size() == size)
                .findFirst()
                .map(SizeVariant::productId)
                .orElse(null);
    }

}
