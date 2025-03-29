package org.peter_lukas.shirtso.commercial.payment;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentDetails {

    private String cardNumber;
    private String cardHolderName;
    private String expiryDate;
    private String cvv;
}
