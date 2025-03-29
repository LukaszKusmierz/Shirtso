package org.peter_lukas.shirtso.commercial.payment.dto;

import org.peter_lukas.shirtso.commercial.payment.PaymentMethod;
import org.peter_lukas.shirtso.commercial.payment.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponseDto(
        Integer paymentId,
        Integer orderId,
        BigDecimal amount,
        PaymentStatus status,
        PaymentMethod paymentMethod,
        String transactionId,
        LocalDateTime paymentDate
) {
}
