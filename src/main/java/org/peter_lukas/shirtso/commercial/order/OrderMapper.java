package org.peter_lukas.shirtso.commercial.order;

import org.peter_lukas.shirtso.commercial.order.dto.OrderDto;
import org.peter_lukas.shirtso.commercial.order.dto.OrderItemDto;
import org.peter_lukas.shirtso.commercial.order.dto.OrderSummaryDto;
import org.peter_lukas.shirtso.commercial.shipping.dto.ShippingMethodDto;
import org.peter_lukas.shirtso.customer.dto.AddressDto;
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

        ShippingMethodDto shippingMethodDto = null;
        if (order.getShippingMethod() != null) {
            shippingMethodDto = new ShippingMethodDto(
                    order.getShippingMethod().getShippingMethodId(),
                    order.getShippingMethod().getName(),
                    order.getShippingMethod().getDescription(),
                    order.getShippingMethod().getPrice(),
                    order.getShippingMethod().getEstimatedDeliveryDays()
            );
        }

        AddressDto shippingAddressDto = null;
        if (order.getShippingAddress() != null) {
            shippingAddressDto = new AddressDto(
                    order.getShippingAddress().getAddressId(),
                    order.getShippingAddress().getUser().getUserId(),
                    order.getShippingAddress().getFullName(),
                    order.getShippingAddress().getStreetAddress(),
                    order.getShippingAddress().getCity(),
                    order.getShippingAddress().getPostalCode(),
                    order.getShippingAddress().getCountry(),
                    order.getShippingAddress().getPhone(),
                    order.getShippingAddress().isDefault()
            );
        }

        return new OrderDto(
                order.getOrderId(),
                order.getUser().getUserId(),
                order.getUser().getUserName(),
                order.getOrderStatus(),
                order.getSubtotalAmount(),
                order.getShippingAmount(),
                order.getDiscountAmount(),
                order.getTaxAmount(),
                order.getTotalAmount(),
                order.getPromoCode(),
                shippingMethodDto,
                shippingAddressDto,
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

        ShippingMethodDto shippingMethodDto = null;
        if (order.getShippingMethod() != null) {
            shippingMethodDto = new ShippingMethodDto(
                    order.getShippingMethod().getShippingMethodId(),
                    order.getShippingMethod().getName(),
                    order.getShippingMethod().getDescription(),
                    order.getShippingMethod().getPrice(),
                    order.getShippingMethod().getEstimatedDeliveryDays()
            );
        }

        return new OrderSummaryDto(
                order.getOrderId(),
                order.getOrderStatus(),
                order.getCreatedAt().format(DATE_FORMATTER),
                order.getSubtotalAmount(),
                order.getShippingAmount(),
                order.getDiscountAmount(),
                order.getTaxAmount(),
                order.getTotalAmount(),
                order.getPromoCode(),
                shippingMethodDto,
                itemCount
        );
    }
}
