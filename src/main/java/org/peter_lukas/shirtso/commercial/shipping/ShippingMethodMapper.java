package org.peter_lukas.shirtso.commercial.shipping;

import org.peter_lukas.shirtso.commercial.shipping.dto.CreateShippingMethodDto;
import org.peter_lukas.shirtso.commercial.shipping.dto.ShippingMethodDto;
import org.peter_lukas.shirtso.commercial.shipping.dto.UpdateShippingMethodDto;
import org.springframework.stereotype.Component;

@Component
public class ShippingMethodMapper {

    public ShippingMethodDto mapToDto(ShippingMethod shippingMethod) {
        return new ShippingMethodDto(
                shippingMethod.getShippingMethodId(),
                shippingMethod.getName(),
                shippingMethod.getDescription(),
                shippingMethod.getPrice(),
                shippingMethod.getEstimatedDeliveryDays(),
                shippingMethod.isActive()
        );
    }

    public ShippingMethod createEntityFromDto(CreateShippingMethodDto dto) {
        return new ShippingMethod(
                dto.name(),
                dto.description(),
                dto.price(),
                dto.estimatedDeliveryDays()
        );
    }

    public void updateEntityFromDto(ShippingMethod shippingMethod, UpdateShippingMethodDto dto) {
        shippingMethod.setName(dto.name());
        shippingMethod.setDescription(dto.description());
        shippingMethod.setPrice(dto.price());
        shippingMethod.setEstimatedDeliveryDays(dto.estimatedDeliveryDays());
        shippingMethod.setActive(dto.isActive());
    }
}
