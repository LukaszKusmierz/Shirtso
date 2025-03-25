package org.peter_lukas.shirtso.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.ResourceResolver;
import org.springframework.web.servlet.resource.ResourceResolverChain;

import java.util.List;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new FallbackResourceResolver());
    }

    /**
     * Custom ResourceResolver that provides a fallback image when the requested image is not found.
     */
    private static class FallbackResourceResolver implements ResourceResolver {
        private final Resource fallbackImage;

        public FallbackResourceResolver() {
            this.fallbackImage = new ClassPathResource("static/placeholder-product.png");
        }

        @Override
        public Resource resolveResource(HttpServletRequest request, String requestPath,
                                        List<? extends Resource> locations, ResourceResolverChain chain) {
            // Try to resolve using the standard mechanism first
            Resource resource = chain.resolveResource(request, requestPath, locations);

            // If the resource doesn't exist and it seems to be an image request, return the fallback
            if (resource == null && isImagePath(requestPath)) {
                return fallbackImage;
            }

            return resource;
        }

        @Override
        public String resolveUrlPath(String resourcePath, List<? extends Resource> locations,
                                     ResourceResolverChain chain) {
            // Use standard URL resolution
            return chain.resolveUrlPath(resourcePath, locations);
        }

        private boolean isImagePath(String path) {
            String lowerPath = path.toLowerCase();
            return lowerPath.endsWith(".jpg") || lowerPath.endsWith(".jpeg") ||
                    lowerPath.endsWith(".png") || lowerPath.endsWith(".gif") ||
                    lowerPath.endsWith(".webp") || lowerPath.endsWith(".svg") ||
                    lowerPath.endsWith(".bmp");
        }
    }
}
