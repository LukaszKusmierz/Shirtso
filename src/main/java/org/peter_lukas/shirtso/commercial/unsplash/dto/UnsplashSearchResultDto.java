package org.peter_lukas.shirtso.commercial.unsplash.dto;

import java.util.List;

public record UnsplashSearchResultDto(
        int total,
        int totalPages,
        List<UnsplashPhotoDto> results
) {
}
