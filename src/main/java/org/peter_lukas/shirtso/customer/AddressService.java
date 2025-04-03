package org.peter_lukas.shirtso.customer;

import org.peter_lukas.shirtso.auth.registration.UserNotFoundException;
import org.peter_lukas.shirtso.auth.user.User;
import org.peter_lukas.shirtso.auth.user.UserRepository;
import org.peter_lukas.shirtso.commercial.product.validation.AddressNotFoundException;
import org.peter_lukas.shirtso.customer.dto.AddressDto;
import org.peter_lukas.shirtso.customer.dto.CreateAddressDto;
import org.peter_lukas.shirtso.customer.dto.UpdateAddressDto;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.peter_lukas.shirtso.messages.Alerts.ADDRESS_NOT_FOUND;
import static org.peter_lukas.shirtso.messages.Alerts.USER_NOT_FOUND;

@Service
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final AddressMapper addressMapper;

    public AddressService(AddressRepository addressRepository,
                          UserRepository userRepository,
                          AddressMapper addressMapper) {
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
        this.addressMapper = addressMapper;
    }

    @Transactional(readOnly = true)
    public List<AddressDto> getUserAddresses() throws UserNotFoundException {
        User currentUser = getCurrentUser();
        return addressRepository.findByUserUserIdOrderByIsDefaultDescCreatedAtDesc(currentUser.getUserId())
                .stream()
                .map(addressMapper::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AddressDto getAddress(Integer addressId) throws UserNotFoundException, AddressNotFoundException {
        User currentUser = getCurrentUser();
        Address address = addressRepository.findByAddressIdAndUserUserId(addressId, currentUser.getUserId())
                .orElseThrow(() -> new AddressNotFoundException(ADDRESS_NOT_FOUND));
        return addressMapper.mapToDto(address);
    }

    @Transactional
    public AddressDto createAddress(CreateAddressDto createAddressDto) throws UserNotFoundException {
        User currentUser = getCurrentUser();
        if (createAddressDto.isDefault()) {
            resetDefaultAddress(currentUser.getUserId());
        }
        Address address = addressMapper.createEntityFromDto(createAddressDto, currentUser);
        Address savedAddress = addressRepository.save(address);
        return addressMapper.mapToDto(savedAddress);
    }

    @Transactional
    public AddressDto updateAddress(Integer addressId, UpdateAddressDto updateAddressDto)
            throws UserNotFoundException, AddressNotFoundException {
        User currentUser = getCurrentUser();
        Address address = addressRepository.findByAddressIdAndUserUserId(addressId, currentUser.getUserId())
                .orElseThrow(() -> new AddressNotFoundException(ADDRESS_NOT_FOUND));
        if (updateAddressDto.isDefault() && !address.isDefault()) {
            resetDefaultAddress(currentUser.getUserId());
        }
        addressMapper.updateEntityFromDto(address, updateAddressDto);
        address.setUpdatedAt(LocalDateTime.now());
        Address updatedAddress = addressRepository.save(address);
        return addressMapper.mapToDto(updatedAddress);
    }

    @Transactional
    public void deleteAddress(Integer addressId) throws UserNotFoundException, AddressNotFoundException {
        User currentUser = getCurrentUser();
        Address address = addressRepository.findByAddressIdAndUserUserId(addressId, currentUser.getUserId())
                .orElseThrow(() -> new AddressNotFoundException(ADDRESS_NOT_FOUND));
        addressRepository.delete(address);
    }

    @Transactional
    public AddressDto setDefaultAddress(Integer addressId) throws UserNotFoundException, AddressNotFoundException {
        User currentUser = getCurrentUser();
        resetDefaultAddress(currentUser.getUserId());
        Address address = addressRepository.findByAddressIdAndUserUserId(addressId, currentUser.getUserId())
                .orElseThrow(() -> new AddressNotFoundException(ADDRESS_NOT_FOUND));
        address.setDefault(true);
        address.setUpdatedAt(LocalDateTime.now());
        Address updatedAddress = addressRepository.save(address);
        return addressMapper.mapToDto(updatedAddress);
    }

    @Transactional(readOnly = true)
    public AddressDto getDefaultAddress() throws UserNotFoundException, AddressNotFoundException {
        User currentUser = getCurrentUser();
        Address address = addressRepository.findDefaultAddress(currentUser.getUserId())
                .orElseThrow(() -> new AddressNotFoundException("No default address found"));
        return addressMapper.mapToDto(address);
    }

    private void resetDefaultAddress(UUID userId) {
        addressRepository.findDefaultAddress(userId).ifPresent(address -> {
            address.setDefault(false);
            address.setUpdatedAt(LocalDateTime.now());
            addressRepository.save(address);
        });
    }

    private User getCurrentUser() throws UserNotFoundException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND));
    }
}
