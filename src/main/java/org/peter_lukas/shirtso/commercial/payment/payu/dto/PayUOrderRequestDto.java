package org.peter_lukas.shirtso.commercial.payment.payu.dto;

import java.util.List;

public record PayUOrderRequestDto(
        String notifyUrl,
        String continueUrl,
        String customerIp,
        String merchantPosId,
        String description,
        String currencyCode,
        String totalAmount,
        String extOrderId,
        PayUBuyer buyer,
        List<PayUProduct> products,
        PayUPayMethods payMethods
) {
    public record PayUBuyer(
            String email,
            String phone,
            String firstName,
            String lastName,
            String language
    ) {
    }

    public record PayUProduct(
            String name,
            String unitPrice,
            String quantity
    ) {
    }

    public record PayUPayMethods(
            PayUPayMethod payMethod
    ) {
    }

    public record PayUPayMethod(
            String type,
            String value,
            PayUCardData card
    ) {
    }

    public record PayUCardData(
            String number,
            String expirationMonth,
            String expirationYear,
            String cvv
    ) {
    }
}
