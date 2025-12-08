package org.peter_lukas.shirtso.commercial.payment.payu.dto;

public record PayUOrderResponseDto(
        PayUStatus status,
        String redirectUri,
        String orderId,
        String extOrderId
) {
    public record PayUStatus(
            String statusCode,
            String statusDesc
    ) {
    }
}
