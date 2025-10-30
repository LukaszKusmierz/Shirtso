package org.peter_lukas.shirtso.commercial.unsplash.dto;

public record UnsplashUserDto(
        String id,
        String username,
        String name,
        String portfolioUrl,
        String bio,
        UnsplashUserLinksDto links
) {
}
