package org.peter_lukas.shirtso.commercial.unsplash.dto;

import com.google.gson.annotations.SerializedName;

public record UnsplashUserDto(
        String id,
        String username,
        String name,
        @SerializedName("portfolio_url")
        String portfolioUrl,
        String bio,
        @SerializedName("profile_image")
        UnsplashProfileImageDto profileImage,
        UnsplashUserLinksDto links
) {
}
