package org.peter_lukas.shirtso.commercial.cart;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.peter_lukas.shirtso.auth.user.User;
import org.peter_lukas.shirtso.commercial.product.Product;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ShoppingCartTest {

    private ShoppingCart shoppingCart;
    private User testUser;
    private Product testProduct1;
    private Product testProduct2;
    private CartItem testCartItem1;
    private CartItem testCartItem2;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(UUID.randomUUID());
        testUser.setEmail("test@example.com");

        testProduct1 = new Product();
        testProduct1.setProductId(UUID.randomUUID());
        testProduct1.setProductName("Test Product 1");
        testProduct1.setPrice(new BigDecimal("19.99"));

        testProduct2 = new Product();
        testProduct2.setProductId(UUID.randomUUID());
        testProduct2.setProductName("Test Product 2");
        testProduct2.setPrice(new BigDecimal("29.99"));

        shoppingCart = new ShoppingCart(testUser);

        testCartItem1 = new CartItem(shoppingCart, testProduct1, 1);
        testCartItem1.setCartItemId(1);
        testCartItem2 = new CartItem(shoppingCart, testProduct2, 2);
        testCartItem2.setCartItemId(2);
    }

    @Test
    void removeItem_WhenItemExistsInCart_RemovesItemAndSetsCartToNull() {
        // given:
        shoppingCart.addItem(testCartItem1);
        assertEquals(1, shoppingCart.getItems().size());
        assertEquals(shoppingCart, testCartItem1.getCart());

        // when:
        shoppingCart.removeItem(testCartItem1);

        // then:
        assertTrue(shoppingCart.getItems().isEmpty());
        assertNull(testCartItem1.getCart());
    }

    @Test
    void removeItem_WithMultipleItems_RemovesOnlySpecifiedItem() {
        // given:
        shoppingCart.addItem(testCartItem1);
        shoppingCart.addItem(testCartItem2);
        assertEquals(2, shoppingCart.getItems().size());

        // when:
        shoppingCart.removeItem(testCartItem1);

        // then:
        assertEquals(1, shoppingCart.getItems().size());
        assertFalse(shoppingCart.getItems().contains(testCartItem1));
        assertTrue(shoppingCart.getItems().contains(testCartItem2));
        assertNull(testCartItem1.getCart());
        assertEquals(shoppingCart, testCartItem2.getCart());
    }

    @Test
    void removeItem_WhenItemNotInCart_DoesNothing() {
        // given:
        shoppingCart.addItem(testCartItem2);
        assertEquals(1, shoppingCart.getItems().size());

        // when:
        shoppingCart.removeItem(testCartItem1);

        // then:
        assertEquals(1, shoppingCart.getItems().size());
        assertTrue(shoppingCart.getItems().contains(testCartItem2));
    }

    @Test
    void removeItem_WhenCartIsEmpty_DoesNothing() {
        // given:
        assertTrue(shoppingCart.getItems().isEmpty());

        // when: Try to remove item from empty cart
        shoppingCart.removeItem(testCartItem1);

        // then:
        assertTrue(shoppingCart.getItems().isEmpty());
    }

    @Test
    void removeItem_WithNullItem_DoesNotThrowException() {
        // given:
        shoppingCart.addItem(testCartItem1);
        assertEquals(1, shoppingCart.getItems().size());

        // when:
        assertDoesNotThrow(() -> shoppingCart.removeItem(null));

        // then:
        assertEquals(1, shoppingCart.getItems().size());
        assertTrue(shoppingCart.getItems().contains(testCartItem1));
    }

    @Test
    void removeItem_AfterRemoval_ItemCannotBeRemovedAgain() {
        // given:
        shoppingCart.addItem(testCartItem1);
        shoppingCart.removeItem(testCartItem1);
        assertTrue(shoppingCart.getItems().isEmpty());

        // when:
        shoppingCart.removeItem(testCartItem1);

        // then:
        assertTrue(shoppingCart.getItems().isEmpty());
        assertNull(testCartItem1.getCart());
    }

    @Test
    void removeItem_VerifyBidirectionalRelationship() {
        // given:
        shoppingCart.addItem(testCartItem1);
        assertTrue(shoppingCart.getItems().contains(testCartItem1));
        assertEquals(shoppingCart, testCartItem1.getCart());

        // when:
        shoppingCart.removeItem(testCartItem1);

        // then:
        assertFalse(shoppingCart.getItems().contains(testCartItem1));
        assertNull(testCartItem1.getCart());
    }

    @Test
    void addAndRemoveItem_CompleteLifecycle() {
        // given:
        assertTrue(shoppingCart.getItems().isEmpty());

        // when:
        shoppingCart.addItem(testCartItem1);

        // then:
        assertEquals(1, shoppingCart.getItems().size());
        assertTrue(shoppingCart.getItems().contains(testCartItem1));
        assertEquals(shoppingCart, testCartItem1.getCart());

        // when:
        shoppingCart.removeItem(testCartItem1);

        // then:
        assertTrue(shoppingCart.getItems().isEmpty());
        assertNull(testCartItem1.getCart());
    }
}
