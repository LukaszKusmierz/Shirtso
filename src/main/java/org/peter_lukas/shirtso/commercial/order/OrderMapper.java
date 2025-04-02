package org.peter_lukas.shirtso.commercial.order;

import org.peter_lukas.shirtso.commercial.order.dto.OrderDto;
import org.peter_lukas.shirtso.commercial.order.dto.OrderItemDto;
import org.peter_lukas.shirtso.commercial.order.dto.OrderSummaryDto;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderMapper {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public OrderDto mapToOrderDto(Order order) {
        List<OrderItemDto> itemDtos = order.getItems().stream()
                .map(this::mapToOrderItemDto)
                .collect(Collectors.toList());

        return new OrderDto(
                order.getOrderId(),
                order.getUser().getUserId(),
                order.getUser().getUserName(),
                order.getOrderStatus(),
                order.getTotalAmount(),
                order.getCreatedAt(),
                itemDtos
        );
    }

    public OrderItemDto mapToOrderItemDto(OrderItem item) {
        BigDecimal total = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));

        return new OrderItemDto(
                item.getOrderItemId(),
                item.getProduct().getProductId(),
                item.getProduct().getProductName(),
                item.getQuantity(),
                item.getPrice(),
                total
        );
    }

    public OrderSummaryDto mapToOrderSummaryDto(Order order) {
        int itemCount = order.getItems().stream()
                .mapToInt(OrderItem::getQuantity)
                .sum();

        return new OrderSummaryDto(
                order.getOrderId(),
                order.getOrderStatus(),
                order.getCreatedAt().format(DATE_FORMATTER),
                order.getTotalAmount().toString(),
                itemCount
        );
    }
}
