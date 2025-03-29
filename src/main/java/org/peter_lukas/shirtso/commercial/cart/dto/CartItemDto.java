package org.peter_lukas.shirtso.commercial.cart.dto;

import org.peter_lukas.shirtso.commercial.product.dto.ProductDto;

import java.math.BigDecimal;

public record CartItemDto(
        Integer cartItemId,
        ProductDto product,
        Integer quantity,
        BigDecimal totalAmount
) {
}
