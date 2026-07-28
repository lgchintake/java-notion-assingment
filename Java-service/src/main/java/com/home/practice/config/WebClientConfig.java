package com.home.practice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    /**
     * Creates and configures a WebClient bean for communicating with the Node.js API.
     * The base URL is injected from the application configuration.
     *
     * @param builder the WebClient.Builder for custom configuration
     * @param nodeApiBaseUrl the base URL for the Node.js API (injected from configuration)
     * @return a configured WebClient instance for making HTTP requests to the Node API
     */
    @Bean
    public WebClient nodeApiWebClient(
            WebClient.Builder builder,
            @Value("${student-service.node-api-base-url}") String nodeApiBaseUrl
    ) {
        return builder
                .baseUrl(nodeApiBaseUrl)
                .build();
    }

    /**
     * Creates and configures a Jackson ObjectMapper bean for JSON serialization/deserialization.
     *
     * @return a new ObjectMapper instance for handling JSON operations
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
