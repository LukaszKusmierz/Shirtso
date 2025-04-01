package org.peter_lukas.shirtso.commercial.discount;

import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import org.peter_lukas.shirtso.analytics.LogExecutionTime;
import org.peter_lukas.shirtso.commercial.discount.dto.*;
import org.peter_lukas.shirtso.commercial.product.validation.PromoCodeNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.peter_lukas.shirtso.auth.config.SpringSecurityConfig.USER_WRITE;

@RestController
@RequestMapping("/api/promo-codes")
public class PromoCodeController {

    private final PromoCodeService promoCodeService;

    public PromoCodeController(PromoCodeService promoCodeService) {
        this.promoCodeService = promoCodeService;
    }

    @GetMapping
    @LogExecutionTime
    @RolesAllowed(USER_WRITE)
    public ResponseEntity<List<PromoCodeDto>> getAllPromoCodes() {
        List<PromoCodeDto> promoCodes = promoCodeService.getAllPromoCodes();
        return ResponseEntity.ok(promoCodes);
    }

    @GetMapping("/active")
    @LogExecutionTime
    @RolesAllowed(USER_WRITE)
    public ResponseEntity<List<PromoCodeDto>> getActivePromoCodes() {
        List<PromoCodeDto> promoCodes = promoCodeService.getActivePromoCodes();
        return ResponseEntity.ok(promoCodes);
    }

    @GetMapping("/{promoCodeId}")
    @LogExecutionTime
    @RolesAllowed(USER_WRITE)
    public ResponseEntity<PromoCodeDto> getPromoCode(@PathVariable Integer promoCodeId) {
        try {
            PromoCodeDto promoCode = promoCodeService.getPromoCode(promoCodeId);
            return ResponseEntity.ok(promoCode);
        } catch (PromoCodeNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping
    @LogExecutionTime
    @RolesAllowed(USER_WRITE)
    public ResponseEntity<?> createPromoCode(@Valid @RequestBody CreatePromoCodeDto createDto) {
        try {
            PromoCodeDto newPromoCode = promoCodeService.createPromoCode(createDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(newPromoCode);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @PutMapping("/{promoCodeId}")
    @LogExecutionTime
    @RolesAllowed(USER_WRITE)
    public ResponseEntity<?> updatePromoCode(
            @PathVariable Integer promoCodeId,
            @Valid @RequestBody UpdatePromoCodeDto updateDto) {
        try {
            PromoCodeDto updatedPromoCode = promoCodeService.updatePromoCode(promoCodeId, updateDto);
            return ResponseEntity.ok(updatedPromoCode);
        } catch (PromoCodeNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @DeleteMapping("/{promoCodeId}")
    @LogExecutionTime
    @RolesAllowed(USER_WRITE)
    public ResponseEntity<Void> deletePromoCode(@PathVariable Integer promoCodeId) {
        try {
            promoCodeService.deletePromoCode(promoCodeId);
            return ResponseEntity.noContent().build();
        } catch (PromoCodeNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping("/validate")
    @LogExecutionTime
    public ResponseEntity<PromoCodeValidationResultDto> validatePromoCode(
            @Valid @RequestBody ValidatePromoCodeDto validateDto) {
        PromoCodeValidationResultDto result = promoCodeService.validatePromoCode(validateDto);
        return ResponseEntity.ok(result);
    }

    private record ErrorResponse(String message) {
    }
}
