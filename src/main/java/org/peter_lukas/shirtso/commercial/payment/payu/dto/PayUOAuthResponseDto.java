package org.peter_lukas.shirtso.commercial.payment.payu.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PayUOAuthResponseDto(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("token_type") String tokenType,
        @JsonProperty("expires_in") Integer expiresIn,
        @JsonProperty("grant_type") String grantType
) {
}
