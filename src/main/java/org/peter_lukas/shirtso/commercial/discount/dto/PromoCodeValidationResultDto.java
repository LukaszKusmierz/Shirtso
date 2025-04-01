package org.peter_lukas.shirtso.commercial.discount.dto;

import java.math.BigDecimal;

public record PromoCodeValidationResultDto(
        boolean valid,
        String message,
        PromoCodeDto promoCode,
        BigDecimal discountAmount
) {
}
