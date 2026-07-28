package com.home.practice.components;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.home.practice.exception.StudentApiException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class StudentApiClient {

    private final WebClient nodeApiWebClient;
    private final ObjectMapper objectMapper;

    /**
     * Constructs a StudentApiClient with the required WebClient and ObjectMapper dependencies.
     *
     * @param nodeApiWebClient the WebClient configured with the Node.js API base URL
     * @param objectMapper the Jackson ObjectMapper for JSON processing
     */
    public StudentApiClient(WebClient nodeApiWebClient, ObjectMapper objectMapper) {
        this.nodeApiWebClient = nodeApiWebClient;
        this.objectMapper = objectMapper;
    }

    /**
     * Fetches a student's data from the Node.js API by their ID.
     * Forwards authentication cookies and CSRF token headers from the original request.
     *
     * @param id the unique identifier of the student to fetch
     * @param accessToken the access token for authentication
     * @param refreshToken the refresh token for token renewal
     * @param csrfToken the CSRF token for security validation
     * @return a JsonNode containing the student data
     * @throws StudentApiException if the API returns an error status or invalid response
     */
    public JsonNode getStudentById(long id, String accessToken, String refreshToken, String csrfToken) {
        return nodeApiWebClient
                .get()
                .uri("/api/v1/students/{id}", id)
                .headers(headers -> forwardAuthHeaders(headers, accessToken, refreshToken, csrfToken))
                .exchangeToMono(response -> {
                    if (response.statusCode().isError()) {
                        return response.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .flatMap(body -> Mono.error(new StudentApiException(
                                        response.statusCode().value(),
                                        extractErrorMessage(body, response.statusCode())
                                )));
                    }

                    return response.bodyToMono(String.class)
                            .defaultIfEmpty("")
                            .map(raw -> {
                                if (raw.isBlank()) {
                                    throw new StudentApiException(
                                            response.statusCode().value(),
                                            "Node API returned an empty response body"
                                    );
                                }
                                try {
                                    return objectMapper.readTree(raw);
                                } catch (Exception e) {
                                    throw new StudentApiException(
                                            response.statusCode().value(),
                                            "Failed to parse Node API response as JSON: " + raw
                                    );
                                }
                            });
                })
                .block();
    }

    /**
     * Forwards authentication headers to the Node.js API request.
     * Constructs a Cookie header with access and refresh tokens, and adds a CSRF token header.
     *
     * @param headers the HTTP headers object to modify
     * @param accessToken the access token to include in the cookie
     * @param refreshToken the refresh token to include in the cookie
     * @param csrfToken the CSRF token to include as a separate header
     */
    private void forwardAuthHeaders(HttpHeaders headers, String accessToken, String refreshToken, String csrfToken) {
        // Build Cookie header manually since WebClient has no cookie jar by default
        String cookieValue = "accessToken=" + accessToken + "; refreshToken=" + refreshToken;
        headers.add(HttpHeaders.COOKIE, cookieValue);

        // CSRF middleware reads this exact header name
        headers.add("X-CSRF-Token", csrfToken);
    }

    /**
     * Extracts the error message from an API error response.
     * Attempts to parse the response as JSON and extract error or message fields.
     * Falls back to the raw body if parsing fails or fields are not found.
     *
     * @param body the response body as a string
     * @param statusCode the HTTP status code from the response
     * @return the extracted error message or a fallback message
     */
    private String extractErrorMessage(String body, HttpStatusCode statusCode) {
        if (body == null || body.isBlank()) {
            return "Node API returned HTTP " + statusCode.value();
        }

        try {
            JsonNode json = objectMapper.readTree(body);
            if (json.hasNonNull("error")) {
                return json.get("error").asText();
            }
            if (json.hasNonNull("message")) {
                return json.get("message").asText();
            }
        } catch (Exception ignored) {
            return body;
        }

        return body;
    }
}
