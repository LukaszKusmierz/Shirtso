package org.peter_lukas.shirtso.commercial.payment.payu;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.payment.payu")
public record PayUProperties(
        String baseUrl,
        String clientId,
        String clientSecret,
        String posId,
        String secondKey,
        String notifyUrl,
        String continueUrl
) {
}
