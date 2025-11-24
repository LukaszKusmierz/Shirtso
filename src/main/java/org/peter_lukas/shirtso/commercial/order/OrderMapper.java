package org.peter_lukas.shirtso.commercial.order;

import org.peter_lukas.shirtso.commercial.order.dto.OrderDto;
import org.peter_lukas.shirtso.commercial.order.dto.OrderItemDto;
import org.peter_lukas.shirtso.commercial.order.dto.OrderSummaryDto;
import org.peter_lukas.shirtso.commercial.shipping.ShippingMethodMapper;
import org.peter_lukas.shirtso.commercial.shipping.dto.ShippingMethodDto;
import org.peter_lukas.shirtso.customer.Address;
import org.peter_lukas.shirtso.customer.AddressMapper;
import org.peter_lukas.shirtso.customer.dto.AddressDto;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderMapper {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private final ShippingMethodMapper shippingMethodMapper;
    private final AddressMapper addressMapper;

    public OrderMapper(ShippingMethodMapper shippingMethodMapper, AddressMapper addressMapper) {
        this.shippingMethodMapper = shippingMethodMapper;
        this.addressMapper = addressMapper;
    }

    public OrderDto mapToOrderDto(Order order) {
        List<OrderItemDto> itemDtos = order.getItems().stream()
                .map(this::mapToOrderItemDto)
                .collect(Collectors.toList());

        ShippingMethodDto shippingMethodDto = null;
        if (order.getShippingMethod() != null) {
            shippingMethodDto = shippingMethodMapper.mapToDto(order.getShippingMethod());
        }

        AddressDto addressDto = null;
        if (order.getShippingAddress() != null) {
            addressDto = addressMapper.mapToDto(order.getShippingAddress());
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
                order.getCurrency().name(),
                order.getPromoCode(),
                shippingMethodDto,
                addressDto,
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
                total,
                item.getCurrency().name()
        );
    }

    public OrderSummaryDto mapToOrderSummaryDto(Order order) {
        int itemCount = order.getItems().stream()
                .mapToInt(OrderItem::getQuantity)
                .sum();

        String shippingMethodName = order.getShippingMethod() != null ?
                order.getShippingMethod().getName() : "Standard";

        String shippingAddressText = "";
        if (order.getShippingAddress() != null) {
            shippingAddressText = formatAddress(order.getShippingAddress());
        }

        String currency = order.getItems().isEmpty() ? "" :
                order.getItems().iterator().next().getCurrency().name();

        return new OrderSummaryDto(
                order.getOrderId(),
                order.getOrderStatus(),
                order.getCreatedAt().format(DATE_FORMATTER),
                order.getSubtotalAmount().toString(),
                order.getShippingAmount().toString(),
                order.getDiscountAmount().toString(),
                order.getTaxAmount().toString(),
                order.getTotalAmount().toString(),
                currency,
                itemCount,
                shippingMethodName,
                shippingAddressText
        );
    }

    private String formatAddress(Address address) {
        return String.format("%s, %s, %s %s, %s",
                address.getFullName(),
                address.getStreetAddress(),
                address.getPostalCode(),
                address.getCity(),
                address.getCountry());
    }
}
