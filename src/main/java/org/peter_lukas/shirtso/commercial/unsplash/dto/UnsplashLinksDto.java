package org.peter_lukas.shirtso.commercial.unsplash.dto;

import com.google.gson.annotations.SerializedName;

public record UnsplashLinksDto(
        String self,
        String html,
        String download,
        @SerializedName("download_location")
        String downloadLocation
) {
}
