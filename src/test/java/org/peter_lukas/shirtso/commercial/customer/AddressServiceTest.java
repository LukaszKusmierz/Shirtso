package org.peter_lukas.shirtso.commercial.customer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.peter_lukas.shirtso.auth.user.User;
import org.peter_lukas.shirtso.auth.user.UserRepository;
import org.peter_lukas.shirtso.auth.validation.UserNotFoundException;
import org.peter_lukas.shirtso.commercial.product.validation.AddressNotFoundException;
import org.peter_lukas.shirtso.customer.Address;
import org.peter_lukas.shirtso.customer.AddressMapper;
import org.peter_lukas.shirtso.customer.AddressRepository;
import org.peter_lukas.shirtso.customer.AddressService;
import org.peter_lukas.shirtso.customer.dto.AddressDto;
import org.peter_lukas.shirtso.customer.dto.CreateAddressDto;
import org.peter_lukas.shirtso.customer.dto.UpdateAddressDto;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AddressMapper addressMapper;

    @InjectMocks
    private AddressService addressService;

    private User testUser;
    private Address testAddress;
    private AddressDto testAddressDto;

    @BeforeEach
    void setUp() {
        testUser = createTestUser();
        testAddress = createTestAddress();
        testAddressDto = createTestAddressDto();
        setupSecurityContext();
    }

    private User createTestUser() {
        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setEmail("test@example.com");
        user.setUserName("testuser");
        return user;
    }

    private Address createTestAddress() {
        Address address = new Address(
                testUser, "John Doe", "123 Main St",
                "TestCity", "12345", "TestCountry", "1234567890"
        );
        address.setAddressId(1);
        address.setDefault(false);
        return address;
    }

    private AddressDto createTestAddressDto() {
        return new AddressDto(
                1, testUser.getUserId(), "John Doe", "123 Main St",
                "TestCity", "12345", "TestCountry", "1234567890", true
        );
    }

    private void setupSecurityContext() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                testUser.getEmail(), "password"
        );
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void getUserAddresses_ReturnsUserAddressList() throws UserNotFoundException {
        // given
        List<Address> addresses = List.of(testAddress);
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(addressRepository.findByUserUserIdOrderByIsDefaultDescCreatedAtDesc(testUser.getUserId()))
                .thenReturn(addresses);
        when(addressMapper.mapToDto(testAddress)).thenReturn(testAddressDto);

        // when
        List<AddressDto> result = addressService.getUserAddresses();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).addressId()).isEqualTo(1);
    }

    @Test
    void getAddress_WithValidId_ReturnsAddress() throws UserNotFoundException {
        // given
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(addressRepository.findByAddressIdAndUserUserId(1, testUser.getUserId()))
                .thenReturn(Optional.of(testAddress));
        when(addressMapper.mapToDto(testAddress)).thenReturn(testAddressDto);

        // when
        AddressDto result = addressService.getAddress(1);

        // then
        assertThat(result).isNotNull();
        assertThat(result.addressId()).isEqualTo(1);
    }

    @Test
    void getAddress_WithInvalidId_ThrowsException() throws UserNotFoundException {
        // given
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(addressRepository.findByAddressIdAndUserUserId(999, testUser.getUserId()))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> addressService.getAddress(999))
                .isInstanceOf(AddressNotFoundException.class);
    }

    @Test
    void createAddress_WithValidData_CreatesAddress() throws UserNotFoundException {
        // given
        CreateAddressDto createDto = new CreateAddressDto(
                "Jane Doe", "456 Oak St", "NewCity",
                "54321", "NewCountry", "9876543210", false
        );

        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(addressMapper.createEntityFromDto(createDto, testUser)).thenReturn(testAddress);
        when(addressRepository.save(testAddress)).thenReturn(testAddress);
        when(addressMapper.mapToDto(testAddress)).thenReturn(testAddressDto);

        // when
        AddressDto result = addressService.createAddress(createDto);

        // then
        assertThat(result).isNotNull();
        verify(addressRepository).save(testAddress);
    }

    @Test
    void createAddress_WithDefaultFlag_ResetsOtherDefaults() throws UserNotFoundException {
        // given
        CreateAddressDto createDto = new CreateAddressDto(
                "Jane Doe", "456 Oak St", "NewCity",
                "54321", "NewCountry", "9876543210", true
        );

        Address existingDefault = new Address(
                testUser, "Old Default", "789 Elm St",
                "OldCity", "98765", "OldCountry", "5555555555"
        );
        existingDefault.setDefault(true);

        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(addressRepository.findDefaultAddress(testUser.getUserId()))
                .thenReturn(Optional.of(existingDefault));
        when(addressMapper.createEntityFromDto(createDto, testUser)).thenReturn(testAddress);
        when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
        when(addressMapper.mapToDto(testAddress)).thenReturn(testAddressDto);

        // when
        addressService.createAddress(createDto);

        // then
        assertThat(existingDefault.isDefault()).isFalse();
        verify(addressRepository, times(2)).save(any(Address.class));
    }

    @Test
    void updateAddress_WithValidData_UpdatesAddress() throws UserNotFoundException {
        // given
        UpdateAddressDto updateDto = new UpdateAddressDto(
                "Updated Name", "Updated St", "Updated City",
                "99999", "Updated Country", "1111111111", false
        );

        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(addressRepository.findByAddressIdAndUserUserId(1, testUser.getUserId()))
                .thenReturn(Optional.of(testAddress));
        when(addressRepository.save(testAddress)).thenReturn(testAddress);
        when(addressMapper.mapToDto(testAddress)).thenReturn(testAddressDto);

        // when
        AddressDto result = addressService.updateAddress(1, updateDto);

        // then
        assertThat(result).isNotNull();
        verify(addressMapper).updateEntityFromDto(testAddress, updateDto);
        verify(addressRepository).save(testAddress);
    }

    @Test
    void updateAddress_WithDefaultFlag_ResetsOtherDefaults() throws UserNotFoundException {
        // given
        UpdateAddressDto updateDto = new UpdateAddressDto(
                "Updated Name", "Updated St", "Updated City",
                "99999", "Updated Country", "1111111111", true
        );

        Address existingDefault = new Address(
                testUser, "Old Default", "789 Elm St",
                "OldCity", "98765", "OldCountry", "5555555555"
        );
        existingDefault.setDefault(true);

        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(addressRepository.findByAddressIdAndUserUserId(1, testUser.getUserId()))
                .thenReturn(Optional.of(testAddress));
        when(addressRepository.findDefaultAddress(testUser.getUserId()))
                .thenReturn(Optional.of(existingDefault));
        when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
        when(addressMapper.mapToDto(testAddress)).thenReturn(testAddressDto);

        // when
        addressService.updateAddress(1, updateDto);

        // then
        assertThat(existingDefault.isDefault()).isFalse();
        verify(addressRepository, times(2)).save(any(Address.class));
    }

    @Test
    void deleteAddress_WithValidId_DeletesAddress() throws UserNotFoundException {
        // given
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(addressRepository.findByAddressIdAndUserUserId(1, testUser.getUserId()))
                .thenReturn(Optional.of(testAddress));

        // when
        addressService.deleteAddress(1);

        // then
        verify(addressRepository).delete(testAddress);
    }

    @Test
    void deleteAddress_WithInvalidId_ThrowsException() throws UserNotFoundException {
        // given
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(addressRepository.findByAddressIdAndUserUserId(999, testUser.getUserId()))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> addressService.deleteAddress(999))
                .isInstanceOf(AddressNotFoundException.class);
    }

    @Test
    void setDefaultAddress_WithValidId_SetsAsDefault() throws UserNotFoundException {
        // given
        Address existingDefault = new Address(
                testUser, "Old Default", "789 Elm St",
                "OldCity", "98765", "OldCountry", "5555555555"
        );
        existingDefault.setDefault(true);

        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(addressRepository.findDefaultAddress(testUser.getUserId()))
                .thenReturn(Optional.of(existingDefault));
        when(addressRepository.findByAddressIdAndUserUserId(1, testUser.getUserId()))
                .thenReturn(Optional.of(testAddress));
        when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
        when(addressMapper.mapToDto(testAddress)).thenReturn(testAddressDto);

        // when
        addressService.setDefaultAddress(1);

        // then
        assertThat(existingDefault.isDefault()).isFalse();
        assertThat(testAddress.isDefault()).isTrue();
        verify(addressRepository, times(2)).save(any(Address.class));
    }

    @Test
    void getDefaultAddress_WhenExists_ReturnsDefault() throws UserNotFoundException, AddressNotFoundException {
        // given
        testAddress.setDefault(true);
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(addressRepository.findDefaultAddress(testUser.getUserId()))
                .thenReturn(Optional.of(testAddress));
        when(addressMapper.mapToDto(testAddress)).thenReturn(testAddressDto);

        // when
        AddressDto result = addressService.getDefaultAddress();

        // then
        assertThat(result).isNotNull();
        assertThat(result.isDefault()).isTrue();
    }

    @Test
    void getDefaultAddress_WhenNotExists_ThrowsException() throws UserNotFoundException {
        // given
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(addressRepository.findDefaultAddress(testUser.getUserId()))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> addressService.getDefaultAddress())
                .isInstanceOf(AddressNotFoundException.class)
                .hasMessageContaining("No default address");
    }
}