package org.peter_lukas.shirtso.commercial.payment;

public enum PaymentStatus {
    NEW,
    PENDING,
    WAITING_FOR_CONFIRMATION,
    COMPLETED,
    CANCELLED,
    REJECTED,
    FAILED,
    REFUNDED
}
