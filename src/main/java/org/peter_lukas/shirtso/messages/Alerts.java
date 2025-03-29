package org.peter_lukas.shirtso.messages;

public final class Alerts {

    private Alerts() {
    }

    public static final String USER_NOT_FOUND = "User not found";

    public static final String DUPLICATE_PRODUCT = "A product with the same attributes already exists.";

    public static final String PRODUCT_NOT_FOUND = "Product not found.";

    public static final String IMAGE_NOT_FOUND = "Image not found.";

    public static final String INSUFFICIENT_STOCK = "Not enough stock available";

    public static final String CART_NOT_FOUND = "Shopping cart not found";

    public static final String CART_ITEM_NOT_FOUND = "Cart item not found";

    public static final String ORDER_NOT_FOUND = "Order not found";

    public static final String ORDER_STATUS_EXCEPTION = "Cannot cancel order with status: ";

    public static final String EMPTY_CART_ORDER = "Cannot create order from empty cart";

    public static final String PAYMENT_FAILED = "Payment processing failed: ";

    public static final String PAYMENT_FAILED_ORDER_STATUS = "Payment can only be processed for orders in NEW status";

    public static final String PAYMENT_ALREADY_EXISTS = "Payment already exists for this order";
}
