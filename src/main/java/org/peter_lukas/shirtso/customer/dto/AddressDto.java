package org.peter_lukas.shirtso.customer.dto;

import java.util.UUID;

public record AddressDto(
        Integer addressId,
        UUID userId,
        String fullName,
        String streetAddress,
        String city,
        String postalCode,
        String country,
        String phone,
        boolean isDefault
) {
}
