package org.peter_lukas.shirtso.commercial.unsplash.dto;

import com.google.gson.annotations.SerializedName;

public record UnsplashPhotoDto(
        String id,
        String description,
        @SerializedName("alt_description")
        String altDescription,
        UnsplashPhotoUrlsDto urls,
        UnsplashUserDto user,
        int width,
        int height,
        String color,
        UnsplashLinksDto links
) {
}
