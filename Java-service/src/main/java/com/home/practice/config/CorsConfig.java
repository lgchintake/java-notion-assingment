package com.home.practice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private final String allowedOrigin;

    /**
     * Constructs a CorsConfig with the allowed origin configuration.
     *
     * @param allowedOrigin the origin URL allowed for CORS requests (injected from configuration)
     */
    public CorsConfig(@Value("${student-service.allowed-origin}") String allowedOrigin) {
        this.allowedOrigin = allowedOrigin;
    }

    /**
     * Configures CORS mappings for the API endpoints.
     * Allows specific HTTP methods (GET, OPTIONS) and headers for cross-origin requests.
     *
     * @param registry the CorsRegistry used to configure CORS settings
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/v1/**")
                .allowedOrigins(allowedOrigin)
                .allowedMethods("GET", "OPTIONS")
                .allowedHeaders("Content-Type", "Accept", "Origin", "Authorization", "X-CSRF-TOKEN")
                .exposedHeaders("Content-Disposition")
                .allowCredentials(true);
    }
}
