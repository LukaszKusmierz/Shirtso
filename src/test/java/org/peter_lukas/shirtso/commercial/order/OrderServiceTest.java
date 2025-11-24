package org.peter_lukas.shirtso.commercial.order;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.peter_lukas.shirtso.auth.user.CurrentUserService;
import org.peter_lukas.shirtso.auth.user.User;
import org.peter_lukas.shirtso.auth.validation.UserNotFoundException;
import org.peter_lukas.shirtso.commercial.cart.CartItem;
import org.peter_lukas.shirtso.commercial.cart.ShoppingCart;
import org.peter_lukas.shirtso.commercial.cart.ShoppingCartRepository;
import org.peter_lukas.shirtso.commercial.discount.PromoCode;
import org.peter_lukas.shirtso.commercial.discount.PromoCodeRepository;
import org.peter_lukas.shirtso.commercial.discount.PromoCodeService;
import org.peter_lukas.shirtso.commercial.order.dto.CreateOrderRequestDto;
import org.peter_lukas.shirtso.commercial.order.dto.OrderDto;
import org.peter_lukas.shirtso.commercial.order.dto.OrderSummaryDto;
import org.peter_lukas.shirtso.commercial.order.dto.UpdateOrderStatusRequestDto;
import org.peter_lukas.shirtso.commercial.product.Product;
import org.peter_lukas.shirtso.commercial.product.ProductRepository;
import org.peter_lukas.shirtso.commercial.product.validation.*;
import org.peter_lukas.shirtso.commercial.shipping.ShippingMethod;
import org.peter_lukas.shirtso.commercial.shipping.ShippingMethodRepository;
import org.peter_lukas.shirtso.customer.Address;
import org.peter_lukas.shirtso.customer.AddressRepository;
import org.peter_lukas.shirtso.notification.NotificationService;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ShoppingCartRepository cartRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private NotificationService notificationService;

    @Mock
    private ShippingMethodRepository shippingMethodRepository;

    @Mock
    private PromoCodeRepository promoCodeRepository;

    @Mock
    private PromoCodeService promoCodeService;

    @Mock
    private AddressRepository addressRepository;

    @InjectMocks
    private OrderService orderService;

    private User testUser;
    private Product testProduct;
    private ShoppingCart testCart;
    private CartItem testCartItem;
    private Order testOrder;
    private ShippingMethod testShippingMethod;
    private Address testAddress;

    @BeforeEach
    void setUp() {
        testUser = createTestUser();
        testProduct = createTestProduct();
        testCart = createTestCart();
        testCartItem = createTestCartItem();
        testOrder = createTestOrder();
        testShippingMethod = createTestShippingMethod();
        testAddress = createTestAddress();
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
        product.setPrice(new BigDecimal("29.99"));
        product.setStock(10);
        return product;
    }

    private ShoppingCart createTestCart() {
        ShoppingCart cart = new ShoppingCart(testUser);
        cart.setCartId(1);
        cart.setUser(testUser);
        return cart;
    }

    private CartItem createTestCartItem() {
        CartItem item = new CartItem(testCart, testProduct, 2);
        item.setCartItemId(1);
        testCart.addItem(item);
        return item;
    }

    private Order createTestOrder() {
        Order order = new Order(testUser);
        order.setOrderId(1);
        order.setOrderStatus(OrderStatus.NEW);
        return order;
    }

    private ShippingMethod createTestShippingMethod() {
        ShippingMethod method = new ShippingMethod();
        method.setShippingMethodId(1);
        method.setName("Standard Shipping");
        method.setPrice(new BigDecimal("9.99"));
        return method;
    }

    private Address createTestAddress() {
        Address address = new Address(testUser, "John Doe", "123 Main St",
                "TestCity", "12345", "TestCountry", "1234567890");
        address.setAddressId(1);
        return address;
    }

    @Test
    void createOrderFromCart_WithValidCart_CreatesOrder() throws UserNotFoundException {
        // given
        CreateOrderRequestDto request = new CreateOrderRequestDto(
                testCart.getCartId(), null, null, null
        );
        OrderDto expectedDto = new OrderDto(
                1, testUser.getUserId(), testUser.getUserName(),
                OrderStatus.NEW, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("59.98"), "USD",
                null, null, null, null, List.of()
        );

        when(currentUserService.getCurrentUser()).thenReturn(testUser);
        when(cartRepository.findById(testCart.getCartId())).thenReturn(Optional.of(testCart));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(orderMapper.mapToOrderDto(any(Order.class))).thenReturn(expectedDto);

        // when
        OrderDto result = orderService.createOrderFromCart(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.orderId()).isEqualTo(1);
        verify(productRepository, times(1)).save(any(Product.class));
        verify(cartRepository).delete(testCart);
        verify(notificationService).sendOrderConfirmationNotification(any(Order.class));
    }

    @Test
    void createOrderFromCart_WithEmptyCart_ThrowsException() throws UserNotFoundException {
        // given
        testCart.getItems().clear();
        CreateOrderRequestDto request = new CreateOrderRequestDto(
                testCart.getCartId(), null, null, null
        );

        when(currentUserService.getCurrentUser()).thenReturn(testUser);
        when(cartRepository.findById(testCart.getCartId())).thenReturn(Optional.of(testCart));

        // when & then
        assertThatThrownBy(() -> orderService.createOrderFromCart(request))
                .isInstanceOf(EmptyCartException.class);

        verify(orderRepository, never()).save(any());
        verify(notificationService, never()).sendOrderConfirmationNotification(any());
    }

    @Test
    void createOrderFromCart_WithInsufficientStock_ThrowsException() throws UserNotFoundException {
        // given
        testProduct.setStock(1); // Less than cart quantity (2)
        CreateOrderRequestDto request = new CreateOrderRequestDto(
                testCart.getCartId(), null, null, null
        );

        when(currentUserService.getCurrentUser()).thenReturn(testUser);
        when(cartRepository.findById(testCart.getCartId())).thenReturn(Optional.of(testCart));

        // when & then
        assertThatThrownBy(() -> orderService.createOrderFromCart(request))
                .isInstanceOf(InsufficientStockException.class);

        verify(orderRepository, never()).save(any());
    }

    @Test
    void createOrderFromCart_WithShippingMethod_AddsShippingCost() throws UserNotFoundException {
        // given
        CreateOrderRequestDto request = new CreateOrderRequestDto(
                testCart.getCartId(), testShippingMethod.getShippingMethodId(), null, null
        );

        when(currentUserService.getCurrentUser()).thenReturn(testUser);
        when(cartRepository.findById(testCart.getCartId())).thenReturn(Optional.of(testCart));
        when(shippingMethodRepository.findById(testShippingMethod.getShippingMethodId()))
                .thenReturn(Optional.of(testShippingMethod));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderMapper.mapToOrderDto(any(Order.class))).thenReturn(mock(OrderDto.class));

        // when
        orderService.createOrderFromCart(request);

        // then
        verify(shippingMethodRepository).findById(testShippingMethod.getShippingMethodId());
    }

    @Test
    void createOrderFromCart_WithPromoCode_AppliesDiscount() throws UserNotFoundException {
        // given
        PromoCode promoCode = mock(PromoCode.class);
        when(promoCode.getCode()).thenReturn("TEST10");
        when(promoCode.isValid(any(BigDecimal.class))).thenReturn(true);
        when(promoCode.calculateDiscount(any(BigDecimal.class)))
                .thenReturn(new BigDecimal("5.99"));

        CreateOrderRequestDto request = new CreateOrderRequestDto(
                testCart.getCartId(), null, null, "TEST10"
        );

        when(currentUserService.getCurrentUser()).thenReturn(testUser);
        when(cartRepository.findById(testCart.getCartId())).thenReturn(Optional.of(testCart));
        when(promoCodeRepository.findByCodeIgnoreCase("TEST10")).thenReturn(Optional.of(promoCode));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderMapper.mapToOrderDto(any(Order.class))).thenReturn(mock(OrderDto.class));
        doNothing().when(promoCodeService).incrementPromoCodeUsage("TEST10");

        // when
        orderService.createOrderFromCart(request);

        // then
        verify(promoCodeRepository).findByCodeIgnoreCase("TEST10");
        verify(promoCodeService).incrementPromoCodeUsage("TEST10");
    }

    @Test
    void getUserOrders_ReturnsUserOrdersList() throws UserNotFoundException {
        // given
        List<Order> orders = List.of(testOrder);
        List<OrderSummaryDto> expectedDtos = List.of(mock(OrderSummaryDto.class));

        when(currentUserService.getCurrentUser()).thenReturn(testUser);
        when(orderRepository.findByUserIdOrderByCreatedAtDesc(testUser.getUserId()))
                .thenReturn(orders);
        when(orderMapper.mapToOrderSummaryDto(any(Order.class)))
                .thenReturn(expectedDtos.get(0));

        // when
        List<OrderSummaryDto> result = orderService.getUserOrders();

        // then
        assertThat(result).hasSize(1);
        verify(orderRepository).findByUserIdOrderByCreatedAtDesc(testUser.getUserId());
    }

    @Test
    void getOrderDetails_WithValidOrder_ReturnsOrderDto() throws UserNotFoundException {
        // given
        OrderDto expectedDto = mock(OrderDto.class);

        when(currentUserService.getCurrentUser()).thenReturn(testUser);
        when(orderRepository.findByOrderIdAndUserIdWithItems(1, testUser.getUserId()))
                .thenReturn(Optional.of(testOrder));
        when(orderMapper.mapToOrderDto(testOrder)).thenReturn(expectedDto);

        // when
        OrderDto result = orderService.getOrderDetails(1);

        // then
        assertThat(result).isNotNull();
        verify(orderRepository).findByOrderIdAndUserIdWithItems(1, testUser.getUserId());
    }

    @Test
    void getOrderDetails_WithNonExistentOrder_ThrowsException() throws UserNotFoundException {
        // given
        when(currentUserService.getCurrentUser()).thenReturn(testUser);
        when(orderRepository.findByOrderIdAndUserIdWithItems(999, testUser.getUserId()))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> orderService.getOrderDetails(999))
                .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    void updateOrderStatus_WithValidOrder_UpdatesStatus() {
        // given
        UpdateOrderStatusRequestDto request = new UpdateOrderStatusRequestDto(OrderStatus.PROCESSING);
        OrderDto expectedDto = mock(OrderDto.class);

        when(orderRepository.findByOrderIdWithItems(1)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(testOrder)).thenReturn(testOrder);
        when(orderMapper.mapToOrderDto(testOrder)).thenReturn(expectedDto);

        // when
        OrderDto result = orderService.updateOrderStatus(1, request);

        // then
        assertThat(result).isNotNull();
        assertThat(testOrder.getOrderStatus()).isEqualTo(OrderStatus.PROCESSING);
        verify(notificationService).sendOrderStatusChangeNotification(testOrder, "NEW");
    }

    @Test
    void cancelOrder_WithNewOrder_CancelsAndRestoresStock() throws UserNotFoundException {
        // given
        OrderItem orderItem = new OrderItem(testOrder, testProduct, 2);
        testOrder.addItem(orderItem);
        long originalStock = testProduct.getStock();

        when(currentUserService.getCurrentUser()).thenReturn(testUser);
        when(orderRepository.findByOrderIdAndUserIdWithItems(1, testUser.getUserId()))
                .thenReturn(Optional.of(testOrder));

        // when
        orderService.cancelOrder(1);

        // then
        assertThat(testOrder.getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(testProduct.getStock()).isEqualTo(originalStock + 2);
        verify(productRepository).save(testProduct);
        verify(notificationService).sendOrderStatusChangeNotification(testOrder, "NEW");
    }

    @Test
    void cancelOrder_WithShippedOrder_ThrowsException() throws UserNotFoundException {
        // given
        testOrder.setOrderStatus(OrderStatus.SHIPPED);

        when(currentUserService.getCurrentUser()).thenReturn(testUser);
        when(orderRepository.findByOrderIdAndUserIdWithItems(1, testUser.getUserId()))
                .thenReturn(Optional.of(testOrder));

        // when & then
        assertThatThrownBy(() -> orderService.cancelOrder(1))
                .isInstanceOf(OrderStatusException.class);

        verify(productRepository, never()).save(any());
    }

    @Test
    void cancelOrder_WithNonExistentOrder_ThrowsException() throws UserNotFoundException {
        // given
        when(currentUserService.getCurrentUser()).thenReturn(testUser);
        when(orderRepository.findByOrderIdAndUserIdWithItems(999, testUser.getUserId()))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> orderService.cancelOrder(999))
                .isInstanceOf(OrderNotFoundException.class);
    }
}