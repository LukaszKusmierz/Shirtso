package org.peter_lukas.shirtso.commercial.unsplash.dto;

public record UnsplashPhotoUrlsDto(
        String raw,
        String full,
        String regular,
        String small,
        String thumb
) {
}
