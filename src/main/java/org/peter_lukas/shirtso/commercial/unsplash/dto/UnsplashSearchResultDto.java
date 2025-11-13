package org.peter_lukas.shirtso.commercial.unsplash.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public record UnsplashSearchResultDto(
        int total,
        @SerializedName("total_pages")
        int totalPages,
        List<UnsplashPhotoDto> results
) {
}
