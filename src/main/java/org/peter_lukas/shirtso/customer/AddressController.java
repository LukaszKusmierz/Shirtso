package org.peter_lukas.shirtso.customer;

import jakarta.validation.Valid;
import org.peter_lukas.shirtso.analytics.LogExecutionTime;
import org.peter_lukas.shirtso.auth.validation.UserNotFoundException;
import org.peter_lukas.shirtso.commercial.product.validation.AddressNotFoundException;
import org.peter_lukas.shirtso.customer.dto.AddressDto;
import org.peter_lukas.shirtso.customer.dto.CreateAddressDto;
import org.peter_lukas.shirtso.customer.dto.UpdateAddressDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping
    @LogExecutionTime
    public ResponseEntity<List<AddressDto>> getUserAddresses() {
        try {
            List<AddressDto> addresses = addressService.getUserAddresses();
            return ResponseEntity.ok(addresses);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @GetMapping("/{addressId}")
    @LogExecutionTime
    public ResponseEntity<AddressDto> getAddress(@PathVariable Integer addressId) {
        try {
            AddressDto address = addressService.getAddress(addressId);
            return ResponseEntity.ok(address);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (AddressNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/default")
    @LogExecutionTime
    public ResponseEntity<AddressDto> getDefaultAddress() {
        try {
            AddressDto address = addressService.getDefaultAddress();
            return ResponseEntity.ok(address);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (AddressNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping
    @LogExecutionTime
    public ResponseEntity<AddressDto> createAddress(@Valid @RequestBody CreateAddressDto createAddressDto) {
        try {
            AddressDto newAddress = addressService.createAddress(createAddressDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(newAddress);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PutMapping("/{addressId}")
    @LogExecutionTime
    public ResponseEntity<AddressDto> updateAddress(
            @PathVariable Integer addressId,
            @Valid @RequestBody UpdateAddressDto updateAddressDto) {
        try {
            AddressDto updatedAddress = addressService.updateAddress(addressId, updateAddressDto);
            return ResponseEntity.ok(updatedAddress);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (AddressNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @DeleteMapping("/{addressId}")
    @LogExecutionTime
    public ResponseEntity<Void> deleteAddress(@PathVariable Integer addressId) {
        try {
            addressService.deleteAddress(addressId);
            return ResponseEntity.noContent().build();
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (AddressNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/{addressId}/default")
    @LogExecutionTime
    public ResponseEntity<AddressDto> setDefaultAddress(@PathVariable Integer addressId) {
        try {
            AddressDto updatedAddress = addressService.setDefaultAddress(addressId);
            return ResponseEntity.ok(updatedAddress);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (AddressNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
