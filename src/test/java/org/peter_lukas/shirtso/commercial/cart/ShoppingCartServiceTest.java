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

@ExtendWith({MockitoExtension.class, InstancioExtension.class})
class ShoppingCartServiceTest {

    @WithSettings
    private static final Settings settings = Settings.create()
            .set(Keys.BEAN_VALIDATION_ENABLED, true)
            .set(Keys.COLLECTION_MIN_SIZE, 1)
            .set(Keys.COLLECTION_MAX_SIZE, 3);

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
        // Generate test data using Instancio
        testUser = Instancio.of(User.class)
                .set(field(User::getEmail), "test@example.com")
                .create();

        testProduct = Instancio.of(Product.class)
                .set(field(Product::getStock), 10)
                .set(field(Product::getPrice), new BigDecimal("19.99"))
                .create();

        testCartItem = Instancio.of(CartItem.class)
                .set(field(CartItem::getProduct), testProduct)
                .set(field(CartItem::getQuantity), 1)
                .create();

        testCart = Instancio.of(ShoppingCart.class)
                .set(field(ShoppingCart::getUser), testUser)
                .set(field(ShoppingCart::getItems), new HashSet<>(Set.of(testCartItem)))
                .create();

        testCartItem.setCart(testCart);

        testCartDto = Instancio.of(CartDto.class)
                .set(field(CartDto::cartId), testCart.getCartId())
                .set(field(CartDto::userId), testUser.getUserId())
                .set(field(CartDto::items), List.of())
                .set(field(CartDto::totalAmount), BigDecimal.ZERO)
                .set(field(CartDto::totalItems), 0)
                .create();

        // Setup security context
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            testUser.getEmail(), "password");
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void getOrCreateCart_WhenCartExists_ReturnsCart() throws org.peter_lukas.shirtso.auth.registration.UserNotFoundException {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUserIdWithItems(testUser.getUserId()))
                .thenReturn(Optional.of(testCart));
        when(cartMapper.mapCartToDto(testCart)).thenReturn(testCartDto);

        // Act
        CartDto result = shoppingCartService.getOrCreateCart();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.cartId()).isEqualTo(testCart.getCartId());
        assertThat(result.userId()).isEqualTo(testUser.getUserId());
        verify(cartRepository, never()).save(any(ShoppingCart.class));
    }

    @Test
    void getOrCreateCart_WhenCartDoesNotExist_CreatesNewCart() throws UserNotFoundException {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUserIdWithItems(testUser.getUserId()))
                .thenReturn(Optional.empty());
        when(cartRepository.save(any(ShoppingCart.class))).thenReturn(testCart);
        when(cartMapper.mapCartToDto(testCart)).thenReturn(testCartDto);

        // Act
        CartDto result = shoppingCartService.getOrCreateCart();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.cartId()).isEqualTo(testCart.getCartId());
        assertThat(result.userId()).isEqualTo(testUser.getUserId());
        verify(cartRepository).save(any(ShoppingCart.class));
    }

    @Test
    void addToCart_WithNewItem_AddsItemToCart() throws UserNotFoundException {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(productRepository.findById(testProduct.getProductId())).thenReturn(Optional.of(testProduct));
        when(cartRepository.findByUserIdWithItems(testUser.getUserId()))
                .thenReturn(Optional.of(testCart));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(testCartItem);
        when(cartMapper.mapCartToDto(any(ShoppingCart.class))).thenReturn(testCartDto);

        AddToCartDto addToCartDto = new AddToCartDto(testProduct.getProductId(), 2);

        // Act
        CartDto result = shoppingCartService.addToCart(addToCartDto);

        // Assert
        assertThat(result).isNotNull();
        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    void addToCart_WithInsufficientStock_ThrowsException() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(productRepository.findById(testProduct.getProductId())).thenReturn(Optional.of(testProduct));
        
        // Try to add more items than in stock
        AddToCartDto addToCartDto = new AddToCartDto(testProduct.getProductId(), 20);

        // Act & Assert
        assertThatThrownBy(() -> shoppingCartService.addToCart(addToCartDto))
                .isInstanceOf(InsufficientStockException.class);
    }

    @Test
    void updateCartItem_WithValidData_UpdatesItem() throws UserNotFoundException {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUserIdWithItems(testUser.getUserId()))
                .thenReturn(Optional.of(testCart));
        when(cartItemRepository.findById(testCartItem.getCartItemId()))
                .thenReturn(Optional.of(testCartItem));
        when(cartMapper.mapCartToDto(any(ShoppingCart.class))).thenReturn(testCartDto);

        UpdateCartItemDto updateDto = new UpdateCartItemDto(testCartItem.getCartItemId(), 3);

        // Act
        CartDto result = shoppingCartService.updateCartItem(updateDto);
//        TODO fix this

        // Assert
        assertThat(result).isNotNull();
        verify(cartItemRepository).findById(testCartItem.getCartItemId());
        verify(cartItemRepository).save(testCartItem);
        assertThat(testCartItem.getQuantity()).isEqualTo(3);
        assertThat(testCartItem.getCart()).isSameAs(testCart);
    }

    @Test
    void removeCartItem_WithValidItem_RemovesItem() throws UserNotFoundException {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUserIdWithItems(testUser.getUserId()))
                .thenReturn(Optional.of(testCart));
        when(cartItemRepository.findById(testCartItem.getCartItemId()))
                .thenReturn(Optional.of(testCartItem));
        when(cartMapper.mapCartToDto(any(ShoppingCart.class))).thenReturn(testCartDto);

        // Act
        CartDto result = shoppingCartService.removeCartItem(testCartItem.getCartItemId());
//        TODO fix this

        // Assert
        assertThat(result).isNotNull();
        assertThat(testCart.getItems()).isEmpty();
        verify(cartItemRepository).delete(testCartItem);
    }

    @Test
    void clearCart_WithExistingCart_RemovesAllItems() throws UserNotFoundException {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUserIdWithItems(testUser.getUserId()))
                .thenReturn(Optional.of(testCart));

        // Act
        shoppingCartService.clearCart();

        // Assert
        assertThat(testCart.getItems()).isEmpty();
        verify(cartRepository).save(testCart);
    }

    @Test
    void getCurrentUser_WhenUserNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> shoppingCartService.getOrCreateCart())
                .isInstanceOf(org.peter_lukas.shirtso.auth.registration.UserNotFoundException.class);
    }
}
