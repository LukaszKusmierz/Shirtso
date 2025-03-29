package org.peter_lukas.shirtso.commercial.payment.dto;

import jakarta.validation.constraints.NotNull;
import org.peter_lukas.shirtso.commercial.payment.PaymentMethod;

public record ProcessPaymentRequestDto(
        @NotNull Integer orderId,
        @NotNull PaymentMethod paymentMethod,
        String cardNumber,
        String cardHolderName,
        String expiryDate,
        String cvv
) {
}
