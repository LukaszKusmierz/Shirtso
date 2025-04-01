package org.peter_lukas.shirtso.commercial.shipping;

import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import org.peter_lukas.shirtso.analytics.LogExecutionTime;
import org.peter_lukas.shirtso.commercial.product.validation.ShippingMethodNotFoundException;
import org.peter_lukas.shirtso.commercial.shipping.dto.CreateShippingMethodDto;
import org.peter_lukas.shirtso.commercial.shipping.dto.ShippingMethodDto;
import org.peter_lukas.shirtso.commercial.shipping.dto.UpdateShippingMethodDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.peter_lukas.shirtso.auth.config.SpringSecurityConfig.USER_WRITE;

@RestController
@RequestMapping("/api/shipping")
public class ShippingMethodController {

    private final ShippingMethodService shippingService;

    public ShippingMethodController(ShippingMethodService shippingService) {
        this.shippingService = shippingService;
    }

    @GetMapping("/methods")
    @LogExecutionTime
    public ResponseEntity<List<ShippingMethodDto>> getActiveShippingMethods() {
        List<ShippingMethodDto> shippingMethods = shippingService.getActiveShippingMethods();
        return ResponseEntity.ok(shippingMethods);
    }

    @GetMapping("/methods/all")
    @LogExecutionTime
    @RolesAllowed(USER_WRITE)
    public ResponseEntity<List<ShippingMethodDto>> getAllShippingMethods() {
        List<ShippingMethodDto> shippingMethods = shippingService.getAllShippingMethods();
        return ResponseEntity.ok(shippingMethods);
    }

    @GetMapping("/methods/{shippingMethodId}")
    @LogExecutionTime
    public ResponseEntity<ShippingMethodDto> getShippingMethod(@PathVariable Integer shippingMethodId) {
        try {
            ShippingMethodDto shippingMethod = shippingService.getShippingMethod(shippingMethodId);
            return ResponseEntity.ok(shippingMethod);
        } catch (ShippingMethodNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping("/methods")
    @LogExecutionTime
    @RolesAllowed(USER_WRITE)
    public ResponseEntity<ShippingMethodDto> createShippingMethod(@Valid @RequestBody CreateShippingMethodDto createDto) {
        try {
            ShippingMethodDto newShippingMethod = shippingService.createShippingMethod(createDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(newShippingMethod);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @PutMapping("/methods/{shippingMethodId}")
    @LogExecutionTime
    @RolesAllowed(USER_WRITE)
    public ResponseEntity<ShippingMethodDto> updateShippingMethod(
            @PathVariable Integer shippingMethodId,
            @Valid @RequestBody UpdateShippingMethodDto updateDto) {
        try {
            ShippingMethodDto updatedShippingMethod = shippingService.updateShippingMethod(shippingMethodId, updateDto);
            return ResponseEntity.ok(updatedShippingMethod);
        } catch (ShippingMethodNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @DeleteMapping("/methods/{shippingMethodId}")
    @LogExecutionTime
    @RolesAllowed(USER_WRITE)
    public ResponseEntity<Void> deleteShippingMethod(@PathVariable Integer shippingMethodId) {
        try {
            shippingService.deleteShippingMethod(shippingMethodId);
            return ResponseEntity.noContent().build();
        } catch (ShippingMethodNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/methods/{shippingMethodId}/toggle")
    @LogExecutionTime
    @RolesAllowed(USER_WRITE)
    public ResponseEntity<ShippingMethodDto> toggleShippingMethodStatus(@PathVariable Integer shippingMethodId) {
        try {
            ShippingMethodDto updatedShippingMethod = shippingService.toggleShippingMethodStatus(shippingMethodId);
            return ResponseEntity.ok(updatedShippingMethod);
        } catch (ShippingMethodNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
