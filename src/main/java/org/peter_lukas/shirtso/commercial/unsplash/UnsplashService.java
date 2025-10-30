package org.peter_lukas.shirtso.commercial.unsplash;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.peter_lukas.shirtso.commercial.unsplash.dto.UnsplashPhotoDto;
import org.peter_lukas.shirtso.commercial.unsplash.dto.UnsplashSearchResultDto;
import org.peter_lukas.shirtso.config.UnsplashConfigProperties;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Service
public class UnsplashService {
    private final UnsplashConfigProperties config;
    private final OkHttpClient httpClient;
    private final Gson gson;

    public UnsplashService(UnsplashConfigProperties config) {
        this.config = config;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        this.gson = new GsonBuilder().create();
    }

    public UnsplashSearchResultDto searchPhotos(String query, int page, int perPage) throws IOException {
        String url = String.format("%s/search/photos?query=%s&page=%d&per_page=%d",
                config.getApiUrl(), query, page, perPage);

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Client-ID " + config.getAccessKey())
                .addHeader("Accept-Version", "v1")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response from Unsplash API: " + response);
            }

            String responseBody = response.body().string();
            return gson.fromJson(responseBody, UnsplashSearchResultDto.class);
        }
    }

    public UnsplashPhotoDto getPhoto(String photoId) throws IOException {
        String url = String.format("%s/photos/%s", config.getApiUrl(), photoId);

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Client-ID " + config.getAccessKey())
                .addHeader("Accept-Version", "v1")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response from Unsplash API: " + response);
            }

            String responseBody = response.body().string();
            return gson.fromJson(responseBody, UnsplashPhotoDto.class);
        }
    }

    public void trackDownload(String downloadLocation) throws IOException {
        Request request = new Request.Builder()
                .url(downloadLocation)
                .addHeader("Authorization", "Client-ID " + config.getAccessKey())
                .addHeader("Accept-Version", "v1")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to track download: " + response);
            }
        }
    }
}
