package org.peter_lukas.shirtso.commercial.cart;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.peter_lukas.shirtso.auth.user.User;
import org.peter_lukas.shirtso.commercial.product.Product;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CartItemTest {

    private ShoppingCart testCart;
    private Product testProduct;
    private CartItem cartItem;

    @BeforeEach
    void setUp() {
        User testUser = new User();
        testUser.setUserId(UUID.randomUUID());
        
        testProduct = new Product();
        testProduct.setProductId(UUID.randomUUID());
        testProduct.setProductName("Test Product");
        testProduct.setPrice(new BigDecimal("19.99"));
        testProduct.setStock(10);
        
        testCart = new ShoppingCart(testUser);
        cartItem = new CartItem(testCart, testProduct, 2);
    }

    @Test
    void constructor_SetsPropertiesCorrectly() {
        // then:
        assertEquals(testCart, cartItem.getCart());
        assertEquals(testProduct, cartItem.getProduct());
        assertEquals(2, cartItem.getQuantity());
        assertEquals(new BigDecimal("39.98"), cartItem.getTotalAmount());
    }

    @Test
    void updateQuantity_WithPositiveValue_UpdatesQuantityAndTotal() {
        // when:
        cartItem.updateQuantity(3);
        
        // then:
        assertEquals(3, cartItem.getQuantity());
        assertEquals(new BigDecimal("59.97"), cartItem.getTotalAmount());
    }
}
