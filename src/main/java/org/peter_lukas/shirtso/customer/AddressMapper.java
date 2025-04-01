package org.peter_lukas.shirtso.customer;

import org.peter_lukas.shirtso.auth.user.User;
import org.peter_lukas.shirtso.customer.dto.AddressDto;
import org.peter_lukas.shirtso.customer.dto.CreateAddressDto;
import org.peter_lukas.shirtso.customer.dto.UpdateAddressDto;
import org.springframework.stereotype.Component;

@Component
public class AddressMapper {

    public AddressDto mapToDto(Address address) {
        return new AddressDto(
                address.getAddressId(),
                address.getUser().getUserId(),
                address.getFullName(),
                address.getStreetAddress(),
                address.getCity(),
                address.getPostalCode(),
                address.getCountry(),
                address.getPhone(),
                address.isDefault()
        );
    }

    public Address createEntityFromDto(CreateAddressDto dto, User user) {
        Address address = new Address(
                user,
                dto.fullName(),
                dto.streetAddress(),
                dto.city(),
                dto.postalCode(),
                dto.country(),
                dto.phone()
        );
        address.setDefault(dto.isDefault());
        return address;
    }

    public void updateEntityFromDto(Address address, UpdateAddressDto dto) {
        address.setFullName(dto.fullName());
        address.setStreetAddress(dto.streetAddress());
        address.setCity(dto.city());
        address.setPostalCode(dto.postalCode());
        address.setCountry(dto.country());
        address.setPhone(dto.phone());
        address.setDefault(dto.isDefault());
    }
}
