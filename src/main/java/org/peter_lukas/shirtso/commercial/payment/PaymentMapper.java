package org.peter_lukas.shirtso.commercial.payment;

import org.peter_lukas.shirtso.commercial.payment.dto.PaymentResponseDto;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public PaymentResponseDto mapToDto(Payment payment) {
        return new PaymentResponseDto(
                payment.getPaymentId(),
                payment.getOrder().getOrderId(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getPaymentMethod(),
                payment.getTransactionId(),
                payment.getPaymentDate(),
                null
        );
    }

    public PaymentResponseDto mapToDtoWithRedirect(Payment payment, String redirectUrl) {
        return new PaymentResponseDto(
                payment.getPaymentId(),
                payment.getOrder().getOrderId(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getPaymentMethod(),
                payment.getTransactionId(),
                payment.getPaymentDate(),
                redirectUrl
        );
    }
}
