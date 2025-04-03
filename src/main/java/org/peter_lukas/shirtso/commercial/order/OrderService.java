package org.peter_lukas.shirtso.commercial.order;

import org.peter_lukas.shirtso.auth.registration.UserNotFoundException;
import org.peter_lukas.shirtso.auth.user.User;
import org.peter_lukas.shirtso.auth.user.UserRepository;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.peter_lukas.shirtso.messages.Alerts.*;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ShoppingCartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderMapper orderMapper;
    private final NotificationService notificationService;
    private final ShippingMethodRepository shippingMethodRepository;
    private final PromoCodeRepository promoCodeRepository;
    private final PromoCodeService promoCodeService;
    private final AddressRepository addressRepository;

    public OrderService(OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        ShoppingCartRepository cartRepository,
                        ProductRepository productRepository,
                        UserRepository userRepository,
                        OrderMapper orderMapper,
                        NotificationService notificationService,
                        ShippingMethodRepository shippingMethodRepository,
                        PromoCodeRepository promoCodeRepository,
                        PromoCodeService promoCodeService,
                        AddressRepository addressRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.orderMapper = orderMapper;
        this.notificationService = notificationService;
        this.shippingMethodRepository = shippingMethodRepository;
        this.promoCodeRepository = promoCodeRepository;
        this.promoCodeService = promoCodeService;
        this.addressRepository = addressRepository;
    }

    @Transactional
    public OrderDto createOrderFromCart(CreateOrderRequestDto request) throws UserNotFoundException {
        User currentUser = getCurrentUser();
        ShoppingCart cart = cartRepository.findById(request.cartId())
                .orElseThrow(() -> new CartNotFoundException(CART_NOT_FOUND));

        if (!cart.getUser().getUserId().equals(currentUser.getUserId())) {
            throw new CartNotFoundException(CART_NOT_FOUND + " for user " + currentUser.getUserName());
        }

        if (cart.getItems().isEmpty()) {
            throw new EmptyCartException(EMPTY_CART_ORDER);
        }

        for (var cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            if (product.getStock() < cartItem.getQuantity()) {
                throw new InsufficientStockException(INSUFFICIENT_STOCK + product.getProductName());
            }
        }

        Order order = new Order(currentUser);

        for (var cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();

            OrderItem orderItem = new OrderItem(order, product, cartItem.getQuantity());
            order.addItem(orderItem);

            product.setStock(product.getStock() - cartItem.getQuantity());
            productRepository.save(product);
        }

        if (request.shippingMethodId() != null) {
            ShippingMethod shippingMethod = shippingMethodRepository.findById(request.shippingMethodId())
                    .orElseThrow(() -> new ShippingMethodNotFoundException(SHIPPING_METHOD_NOT_FOUND));
            order.setShippingMethod(shippingMethod);
            order.setShippingAmount(shippingMethod.getPrice());
        }

        if (request.addressId() != null) {
            Address shippingAddress = addressRepository.findById(request.addressId())
                    .orElseThrow(() -> new AddressNotFoundException("Shipping address not found"));
            if (!shippingAddress.getUser().getUserId().equals(currentUser.getUserId())) {
                throw new AddressNotFoundException("Shipping address not found for user");
            }
            order.setShippingAddress(shippingAddress);
        }

        if (request.promoCode() != null && !request.promoCode().isEmpty()) {
            Optional<PromoCode> promoCodeOpt = promoCodeRepository.findByCodeIgnoreCase(request.promoCode());
            if (promoCodeOpt.isPresent()) {
                PromoCode promoCode = promoCodeOpt.get();
                if (promoCode.isValid(order.getSubtotalAmount())) {
                    BigDecimal discountAmount = promoCode.calculateDiscount(order.getSubtotalAmount());
                    order.setPromoCode(promoCode.getCode());
                    order.setDiscountAmount(discountAmount);
                    promoCodeService.incrementPromoCodeUsage(promoCode.getCode());
                }
            }
        }

        Order savedOrder = orderRepository.save(order);
        cartRepository.delete(cart);
        notificationService.sendOrderConfirmationNotification(savedOrder);

        return orderMapper.mapToOrderDto(savedOrder);
    }

    @Transactional(readOnly = true)
    public List<OrderSummaryDto> getUserOrders() throws UserNotFoundException {
        User currentUser = getCurrentUser();

        return orderRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getUserId()).stream()
                .map(orderMapper::mapToOrderSummaryDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrderDto getOrderDetails(Integer orderId) throws UserNotFoundException {
        User currentUser = getCurrentUser();

        Order order = orderRepository.findByOrderIdAndUserIdWithItems(orderId, currentUser.getUserId())
                .orElseThrow(() -> new OrderNotFoundException(ORDER_NOT_FOUND));

        return orderMapper.mapToOrderDto(order);
    }

    @Transactional
    public OrderDto updateOrderStatus(Integer orderId, UpdateOrderStatusRequestDto request) {
        Order order = orderRepository.findByOrderIdWithItems(orderId)
                .orElseThrow(() -> new OrderNotFoundException(ORDER_NOT_FOUND));

        OrderStatus previousStatus = order.getOrderStatus();
        order.setOrderStatus(request.orderStatus());
        Order savedOrder = orderRepository.save(order);
        notificationService.sendOrderStatusChangeNotification(savedOrder, previousStatus.toString());

        return orderMapper.mapToOrderDto(savedOrder);
    }

    @Transactional
    public void cancelOrder(Integer orderId) throws UserNotFoundException {
        User currentUser = getCurrentUser();

        Order order = orderRepository.findByOrderIdAndUserIdWithItems(orderId, currentUser.getUserId())
                .orElseThrow(() -> new OrderNotFoundException(ORDER_NOT_FOUND));

        if (order.getOrderStatus() != OrderStatus.NEW && order.getOrderStatus() != OrderStatus.PROCESSING) {
            throw new OrderStatusException(ORDER_STATUS_EXCEPTION + order.getOrderStatus());
        }

        OrderStatus previousStatus = order.getOrderStatus();
        order.setOrderStatus(OrderStatus.CANCELLED);

        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.setStock(product.getStock() + item.getQuantity());
            productRepository.save(product);
        }

        notificationService.sendOrderStatusChangeNotification(order, previousStatus.toString());
        orderRepository.save(order);
    }

    private User getCurrentUser() throws UserNotFoundException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND));
    }
}

