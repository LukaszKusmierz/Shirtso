package org.peter_lukas.shirtso.commercial.cart;

import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.peter_lukas.shirtso.auth.user.User;
import org.peter_lukas.shirtso.commercial.product.Product;



import static org.junit.jupiter.api.Assertions.*;

class ShoppingCartTest {

    private ShoppingCart shoppingCart;
    private User testUser;
    private Product testProduct;
    private CartItem testCartItem;

    @BeforeEach
    void setUp() {
        testUser = Instancio.create(User.class);
        testProduct = Instancio.create(Product.class);
        shoppingCart = new ShoppingCart(testUser);
        shoppingCart.setCartId(1);
        testCartItem = Instancio.create(CartItem.class);
        testCartItem.setCart(shoppingCart);
        testCartItem.setProduct(testProduct);
        testCartItem.setQuantity(1);
    }

    @Test
    void addItem_AddsItemToCart() {
        shoppingCart.addItem(testCartItem);
        assertEquals(1, shoppingCart.getItems().size());
        assertTrue(shoppingCart.getItems().contains(testCartItem));
        assertEquals(shoppingCart, testCartItem.getCart());
    }

    @Test
    void removeItem_RemovesItemFromCart() {
        shoppingCart.addItem(testCartItem);
        shoppingCart.removeItem(testCartItem);
        assertTrue(shoppingCart.getItems().isEmpty());
        assertNull(testCartItem.getCart());
    }

    @Test
    void removeItem_WhenItemNotInCart_DoesNothing() {
//        given:
        CartItem anotherItem = Instancio.create(CartItem.class);
        anotherItem.setCart(shoppingCart);
        anotherItem.setProduct(testProduct);
        anotherItem.setQuantity(2);
        shoppingCart.addItem(anotherItem);
//        when:
        shoppingCart.removeItem(testCartItem);
//        then:

        assertEquals(1, shoppingCart.getItems().size());
        assertTrue(shoppingCart.getItems().contains(anotherItem));
    }
}
