package org.peter_lukas.shirtso.commercial.cart;

import org.peter_lukas.shirtso.auth.registration.UserNotFoundException;
import org.peter_lukas.shirtso.auth.user.User;
import org.peter_lukas.shirtso.auth.user.UserRepository;
import org.peter_lukas.shirtso.commercial.cart.dto.AddToCartDto;
import org.peter_lukas.shirtso.commercial.cart.dto.CartDto;
import org.peter_lukas.shirtso.commercial.cart.dto.UpdateCartItemDto;
import org.peter_lukas.shirtso.commercial.product.Product;
import org.peter_lukas.shirtso.commercial.product.ProductRepository;
import org.peter_lukas.shirtso.commercial.product.validation.CartItemNotFoundException;
import org.peter_lukas.shirtso.commercial.product.validation.CartNotFoundException;
import org.peter_lukas.shirtso.commercial.product.validation.InsufficientStockException;
import org.peter_lukas.shirtso.commercial.product.validation.ProductNotFoundException;
import org.peter_lukas.shirtso.messages.Alerts;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.peter_lukas.shirtso.messages.Alerts.*;


@Service
public class ShoppingCartService {

    private final ShoppingCartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ShoppingCartMapper cartMapper;

    public ShoppingCartService(ShoppingCartRepository cartRepository,
                               CartItemRepository cartItemRepository,
                               ProductRepository productRepository,
                               UserRepository userRepository,
                               ShoppingCartMapper cartMapper) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.cartMapper = cartMapper;
    }

    @Transactional
    public CartDto getOrCreateCart() throws UserNotFoundException {
        User currentUser = getCurrentUser();
        ShoppingCart cart = cartRepository.findByUser(currentUser)
                .orElseGet(() -> cartRepository.save(new ShoppingCart(currentUser)));

        return cartMapper.mapCartToDto(cart);
    }

    @Transactional
    public CartDto addToCart(AddToCartDto addToCartDto) throws UserNotFoundException {
        User currentUser = getCurrentUser();
        Product product = productRepository.findById(addToCartDto.productId())
                .orElseThrow(() -> new ProductNotFoundException(Alerts.PRODUCT_NOT_FOUND));

        if (product.getStock() < addToCartDto.quantity()) {
            throw new InsufficientStockException(INSUFFICIENT_STOCK);
        }

        ShoppingCart cart = cartRepository.findByUser(currentUser)
                .orElseGet(() -> cartRepository.save(new ShoppingCart(currentUser)));

        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getProductId().equals(addToCartDto.productId()))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + addToCartDto.quantity();

            if (product.getStock() < newQuantity) {
                throw new InsufficientStockException(INSUFFICIENT_STOCK);
            }

            item.updateQuantity(newQuantity);
            cartItemRepository.save(item);
        } else {
            CartItem newItem = new CartItem(cart, product, addToCartDto.quantity());
            cart.addItem(newItem);
            cartItemRepository.save(newItem);
        }

        return cartMapper.mapCartToDto(cart);
    }

    @Transactional
    public CartDto updateCartItem(UpdateCartItemDto updateCartItemDto) throws UserNotFoundException {
        User currentUser = getCurrentUser();
        ShoppingCart cart = cartRepository.findByUser(currentUser)
                .orElseThrow(() -> new CartNotFoundException(CART_NOT_FOUND));

        CartItem cartItem = cartItemRepository.findById(updateCartItemDto.cartItemId())
                .orElseThrow(() -> new CartItemNotFoundException(CART_ITEM_NOT_FOUND));

        if (!cartItem.getCart().getCartId().equals(cart.getCartId())) {
            throw new CartItemNotFoundException(CART_ITEM_NOT_FOUND);
        }

        if (cartItem.getProduct().getStock() < updateCartItemDto.quantity()) {
            throw new InsufficientStockException(INSUFFICIENT_STOCK);
        }

        cartItem.updateQuantity(updateCartItemDto.quantity());
        cartItemRepository.save(cartItem);

        return cartMapper.mapCartToDto(cart);
    }

    @Transactional
    public CartDto removeCartItem(Integer cartItemId) throws UserNotFoundException {
        User currentUser = getCurrentUser();
        ShoppingCart cart = cartRepository.findByUser(currentUser)
                .orElseThrow(() -> new CartNotFoundException(CART_NOT_FOUND));

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new CartItemNotFoundException(CART_ITEM_NOT_FOUND));

        if (!cartItem.getCart().getCartId().equals(cart.getCartId())) {
            throw new CartItemNotFoundException(CART_ITEM_NOT_FOUND);
        }

        cart.removeItem(cartItem);
        cartItemRepository.delete(cartItem);

        return cartMapper.mapCartToDto(cart);
    }

    @Transactional
    public void clearCart() throws UserNotFoundException {
        User currentUser = getCurrentUser();
        ShoppingCart cart = cartRepository.findByUser(currentUser)
                .orElseThrow(() -> new CartNotFoundException(CART_NOT_FOUND));

        cart.getItems().clear();
        cartRepository.save(cart);
    }

    private User getCurrentUser() throws UserNotFoundException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND));
    }
}
