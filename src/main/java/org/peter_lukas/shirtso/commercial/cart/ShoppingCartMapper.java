package org.peter_lukas.shirtso.commercial.cart;

import org.peter_lukas.shirtso.commercial.cart.dto.CartDto;
import org.peter_lukas.shirtso.commercial.cart.dto.CartItemDto;
import org.peter_lukas.shirtso.commercial.product.ProductMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ShoppingCartMapper {

    private final ProductMapper productMapper;

    public ShoppingCartMapper(ProductMapper productMapper) {
        this.productMapper = productMapper;
    }

    public CartDto mapCartToDto(ShoppingCart cart) {
        List<CartItemDto> cartItemDtos = cart.getItems().stream()
                .map(this::mapCartItemToDto)
                .collect(Collectors.toList());

        BigDecimal totalAmount = cartItemDtos.stream()
                .map(CartItemDto::totalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalItems = cartItemDtos.stream()
                .mapToInt(CartItemDto::quantity)
                .sum();

        return new CartDto(
                cart.getCartId(),
                cart.getUser().getUserId(),
                cartItemDtos,
                totalAmount,
                totalItems
        );
    }

    public CartItemDto mapCartItemToDto(CartItem cartItem) {
        return new CartItemDto(
                cartItem.getCartItemId(),
                productMapper.mapProductEntityToDto(cartItem.getProduct()),
                cartItem.getQuantity(),
                cartItem.getTotalAmount()
        );
    }
}
