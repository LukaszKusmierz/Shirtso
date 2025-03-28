package org.peter_lukas.shirtso.commercial.cart;

import jakarta.validation.Valid;
import org.peter_lukas.shirtso.analytics.LogExecutionTime;
import org.peter_lukas.shirtso.auth.registration.UserNotFoundException;
import org.peter_lukas.shirtso.commercial.cart.dto.AddToCartDto;
import org.peter_lukas.shirtso.commercial.cart.dto.CartDto;
import org.peter_lukas.shirtso.commercial.cart.dto.UpdateCartItemDto;
import org.peter_lukas.shirtso.commercial.product.validation.CartItemNotFoundException;
import org.peter_lukas.shirtso.commercial.product.validation.CartNotFoundException;
import org.peter_lukas.shirtso.commercial.product.validation.InsufficientStockException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class ShoppingCartController {

    private final ShoppingCartService cartService;

    public ShoppingCartController(ShoppingCartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    @LogExecutionTime
    public ResponseEntity<CartDto> getCart() {
        try {
            CartDto cart = cartService.getOrCreateCart();
            return ResponseEntity.ok(cart);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping
    @LogExecutionTime
    public ResponseEntity<CartDto> addToCart(@Valid @RequestBody AddToCartDto addToCartDto) {
        try {
            CartDto cart = cartService.addToCart(addToCartDto);
            return ResponseEntity.ok(cart);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (org.peter_lukas.shirtso.commercial.product.validation.ProductNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (InsufficientStockException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @PutMapping
    @LogExecutionTime
    public ResponseEntity<CartDto> updateCartItem(@Valid @RequestBody UpdateCartItemDto updateCartItemDto) {
        try {
            CartDto cart = cartService.updateCartItem(updateCartItemDto);
            return ResponseEntity.ok(cart);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (CartNotFoundException | CartItemNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (InsufficientStockException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @DeleteMapping("/{cartItemId}")
    @LogExecutionTime
    public ResponseEntity<CartDto> removeCartItem(@PathVariable Integer cartItemId) {
        try {
            CartDto cart = cartService.removeCartItem(cartItemId);
            return ResponseEntity.ok(cart);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (CartNotFoundException | CartItemNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @DeleteMapping
    @LogExecutionTime
    public ResponseEntity<Void> clearCart() {
        try {
            cartService.clearCart();
            return ResponseEntity.noContent().build();
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (CartNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
