package org.peter_lukas.shirtso.customer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateAddressDto(
        @NotBlank(message = "Full name is required")
        String fullName,

        @NotBlank(message = "Street address is required")
        String streetAddress,

        @NotBlank(message = "City is required")
        String city,

        @NotBlank(message = "Postal code is required")
        String postalCode,

        @NotBlank(message = "Country is required")
        String country,

        @Pattern(regexp = "^[+]?[(]?[0-9]{1,4}[)]?[-\\s.]?[0-9]{3}[-\\s.]?[0-9]{4,6}$",
                message = "Invalid phone number format")
        String phone,

        boolean isDefault
) {
}
