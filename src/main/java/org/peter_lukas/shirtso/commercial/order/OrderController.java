package org.peter_lukas.shirtso.commercial.order;

import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import org.peter_lukas.shirtso.analytics.LogExecutionTime;
import org.peter_lukas.shirtso.auth.registration.UserNotFoundException;
import org.peter_lukas.shirtso.commercial.order.dto.CreateOrderRequest;
import org.peter_lukas.shirtso.commercial.order.dto.OrderDto;
import org.peter_lukas.shirtso.commercial.order.dto.OrderSummaryDto;
import org.peter_lukas.shirtso.commercial.order.dto.UpdateOrderStatusRequest;
import org.peter_lukas.shirtso.commercial.product.validation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.peter_lukas.shirtso.auth.config.SpringSecurityConfig.USER_WRITE;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @LogExecutionTime
    public ResponseEntity<OrderDto> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        try {
            OrderDto order = orderService.createOrderFromCart(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(order);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (CartNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (EmptyCartException | InsufficientStockException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping
    @LogExecutionTime
    public ResponseEntity<List<OrderSummaryDto>> getUserOrders() {
        try {
            List<OrderSummaryDto> orders = orderService.getUserOrders();
            return ResponseEntity.ok(orders);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @GetMapping("/{orderId}")
    @LogExecutionTime
    public ResponseEntity<OrderDto> getOrderDetails(@PathVariable Integer orderId) {
        try {
            OrderDto order = orderService.getOrderDetails(orderId);
            return ResponseEntity.ok(order);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (OrderNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/{orderId}/status")
    @LogExecutionTime
    @RolesAllowed(USER_WRITE)
    public ResponseEntity<OrderDto> updateOrderStatus(
            @PathVariable Integer orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        try {
            OrderDto order = orderService.updateOrderStatus(orderId, request);
            return ResponseEntity.ok(order);
        } catch (OrderNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping("/{orderId}/cancel")
    @LogExecutionTime
    public ResponseEntity<Void> cancelOrder(@PathVariable Integer orderId) {
        try {
            orderService.cancelOrder(orderId);
            return ResponseEntity.noContent().build();
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (OrderNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (OrderStatusException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
}
