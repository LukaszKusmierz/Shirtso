package org.peter_lukas.shirtso.commercial.payment.payu;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(PayUProperties.class)
public class PayUConfiguration {
}
