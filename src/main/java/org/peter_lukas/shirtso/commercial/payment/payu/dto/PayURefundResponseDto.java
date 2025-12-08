package org.peter_lukas.shirtso.commercial.payment.payu.dto;

public record PayURefundResponseDto(
        PayUStatus status,
        String orderId,
        PayURefund refund
) {
    public record PayUStatus(
            String statusCode,
            String statusDesc
    ) {
    }

    public record PayURefund(
            String refundId,
            String extRefundId,
            String amount,
            String currencyCode,
            String description,
            String creationDateTime,
            String status,
            String statusDateTime
    ) {
    }
}
