package org.peter_lukas.shirtso.commercial.payment.payu.dto;

public record PayURefundRequestDto(
        PayURefund refund
) {
    public record PayURefund(
            String description,
            String amount,
            String extRefundId
    ) {
    }
}
