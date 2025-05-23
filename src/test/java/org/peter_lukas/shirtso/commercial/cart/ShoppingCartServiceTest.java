package org.peter_lukas.shirtso.commercial.cart;

import org.instancio.Instancio;
import org.instancio.junit.InstancioExtension;
import org.instancio.junit.WithSettings;
import org.instancio.settings.Keys;
import org.instancio.settings.Settings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.peter_lukas.shirtso.auth.registration.UserNotFoundException;
import org.peter_lukas.shirtso.auth.user.User;
import org.peter_lukas.shirtso.commercial.cart.dto.AddToCartDto;
import org.peter_lukas.shirtso.commercial.cart.dto.CartDto;
import org.peter_lukas.shirtso.commercial.cart.dto.UpdateCartItemDto;
import org.peter_lukas.shirtso.commercial.product.Product;
import org.peter_lukas.shirtso.commercial.product.ProductRepository;
import org.peter_lukas.shirtso.commercial.product.validation.InsufficientStockException;
import org.peter_lukas.shirtso.auth.user.UserRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class})
class ShoppingCartServiceTest {

    @Mock
    private ShoppingCartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ShoppingCartMapper cartMapper;

    @InjectMocks
    private ShoppingCartService shoppingCartService;

    private User testUser;
    private Product testProduct;
    private ShoppingCart testCart;
    private CartItem testCartItem;
    private CartDto testCartDto;

    @BeforeEach
    void setUp() {
        testUser = createTestUser();
        testProduct = createTestProduct();
        testCart = createTestCart();
        testCartItem = createTestCartItem();
        testCartDto = createTestCartDto();

        setupSecurityContext();
    }

    private User createTestUser() {
        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setEmail("test@example.com");
        user.setUserName("testuser");
        return user;
    }

    private Product createTestProduct() {
        Product product = new Product();
        product.setProductId(UUID.randomUUID());
        product.setProductName("Test Product");
        product.setDescription("Test Description");
        product.setPrice(new BigDecimal("19.99"));
        product.setStock(10);
        return product;
    }

    private ShoppingCart createTestCart() {
        ShoppingCart cart = new ShoppingCart(testUser);
        cart.setCartId(1);
        return cart;
    }

    private CartItem createTestCartItem() {
        CartItem item = new CartItem(testCart, testProduct, 1);
        item.setCartItemId(1);
        return item;
    }

    private CartDto createTestCartDto() {
        return new CartDto(
                testCart.getCartId(),
                testUser.getUserId(),
                List.of(),
                BigDecimal.ZERO,
                0
        );
    }

    private void setupSecurityContext() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                testUser.getEmail(), "password");
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void getOrCreateCart_WhenCartExists_ReturnsCart() throws org.peter_lukas.shirtso.auth.registration.UserNotFoundException {
        // given
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUserIdWithItems(testUser.getUserId()))
                .thenReturn(Optional.of(testCart));
        when(cartMapper.mapCartToDto(testCart)).thenReturn(testCartDto);

        // when
        CartDto result = shoppingCartService.getOrCreateCart();

        // then
        assertThat(result).isNotNull();
        assertThat(result.cartId()).isEqualTo(testCart.getCartId());
        assertThat(result.userId()).isEqualTo(testUser.getUserId());
        verify(cartRepository, never()).save(any(ShoppingCart.class));
    }

    @Test
    void getOrCreateCart_WhenCartDoesNotExist_CreatesNewCart() throws UserNotFoundException {
        // given
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUserIdWithItems(testUser.getUserId()))
                .thenReturn(Optional.empty());
        when(cartRepository.save(any(ShoppingCart.class))).thenReturn(testCart);
        when(cartMapper.mapCartToDto(testCart)).thenReturn(testCartDto);

        // when
        CartDto result = shoppingCartService.getOrCreateCart();

        // then
        assertThat(result).isNotNull();
        assertThat(result.cartId()).isEqualTo(testCart.getCartId());
        assertThat(result.userId()).isEqualTo(testUser.getUserId());
        verify(cartRepository).save(any(ShoppingCart.class));
    }

    @Test
    void addToCart_WithNewItem_AddsItemToCart() throws UserNotFoundException {
        // given
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(productRepository.findById(testProduct.getProductId())).thenReturn(Optional.of(testProduct));
        when(cartRepository.findByUserIdWithItems(testUser.getUserId()))
                .thenReturn(Optional.of(testCart));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(testCartItem);
        when(cartMapper.mapCartToDto(any(ShoppingCart.class))).thenReturn(testCartDto);
        AddToCartDto addToCartDto = new AddToCartDto(testProduct.getProductId(), 2);

        // when
        CartDto result = shoppingCartService.addToCart(addToCartDto);

        // then
        assertThat(result).isNotNull();
        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    void addToCart_WithInsufficientStock_ThrowsException() {
        // given
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(productRepository.findById(testProduct.getProductId())).thenReturn(Optional.of(testProduct));
        AddToCartDto addToCartDto = new AddToCartDto(testProduct.getProductId(), 20);

        // when & then
        assertThatThrownBy(() -> shoppingCartService.addToCart(addToCartDto))
                .isInstanceOf(InsufficientStockException.class);
    }

    @Test
    void updateCartItem_WithValidData_UpdatesItem() throws UserNotFoundException {
        // given
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUserIdWithItems(testUser.getUserId()))
                .thenReturn(Optional.of(testCart));
        when(cartItemRepository.findById(testCartItem.getCartItemId()))
                .thenReturn(Optional.of(testCartItem));
        when(cartMapper.mapCartToDto(any(ShoppingCart.class))).thenReturn(testCartDto);
        UpdateCartItemDto updateDto = new UpdateCartItemDto(testCartItem.getCartItemId(), 3);

        // when
        CartDto result = shoppingCartService.updateCartItem(updateDto);


        // then
        assertThat(result).isNotNull();
        verify(cartItemRepository).findById(testCartItem.getCartItemId());
        verify(cartItemRepository).save(testCartItem);
        assertThat(testCartItem.getQuantity()).isEqualTo(3);
        assertThat(testCartItem.getCart()).isSameAs(testCart);
    }

    @Test
    void removeCartItem_WithValidItem_RemovesItem() throws UserNotFoundException {
        // given
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUserIdWithItems(testUser.getUserId()))
                .thenReturn(Optional.of(testCart));
        when(cartItemRepository.findById(testCartItem.getCartItemId()))
                .thenReturn(Optional.of(testCartItem));
        when(cartMapper.mapCartToDto(any(ShoppingCart.class))).thenReturn(testCartDto);

        // when
        CartDto result = shoppingCartService.removeCartItem(testCartItem.getCartItemId());

        // then
        assertThat(result).isNotNull();
        assertThat(testCart.getItems()).isEmpty();
        verify(cartItemRepository).delete(testCartItem);
    }

    @Test
    void clearCart_WithExistingCart_RemovesAllItems() throws UserNotFoundException {
        // given
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUserIdWithItems(testUser.getUserId()))
                .thenReturn(Optional.of(testCart));

        // when
        shoppingCartService.clearCart();

        // then
        assertThat(testCart.getItems()).isEmpty();
        verify(cartRepository).save(testCart);
    }

    @Test
    void getCurrentUser_WhenUserNotFound_ThrowsException() {
        // when
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // then
        assertThatThrownBy(() -> shoppingCartService.getOrCreateCart())
                .isInstanceOf(org.peter_lukas.shirtso.auth.registration.UserNotFoundException.class);
    }
}
