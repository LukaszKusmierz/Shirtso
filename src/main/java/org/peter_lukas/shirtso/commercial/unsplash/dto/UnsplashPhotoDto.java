package org.peter_lukas.shirtso.commercial.unsplash.dto;

public record UnsplashPhotoDto(
        String id,
        String description,
        String altDescription,
        UnsplashPhotoUrlsDto urls,
        UnsplashUserDto user,
        int width,
        int height,
        String color,
        UnsplashLinksDto links
) {
}
