package com.home.practice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    /**
     * Handles StudentApiException thrown during request processing.
     * Maps the exception to an appropriate HTTP status code and returns an error response.
     *
     * @param error the StudentApiException containing the status code and error message
     * @return a ResponseEntity with the appropriate HTTP status and error details
     */
    @ExceptionHandler(StudentApiException.class)
    public ResponseEntity<Map<String, String>> handleStudentApiException(StudentApiException error) {
        HttpStatus status = HttpStatus.resolve(error.getStatusCode());
        if (status == null) {
            status = HttpStatus.BAD_GATEWAY;
        }
        return ResponseEntity.status(status).body(Map.of("error", error.getMessage()));
    }

    /**
     * Handles generic exceptions that may occur during request processing.
     * Returns a 500 Internal Server Error status with a generic error message.
     *
     * @param error the generic Exception that was thrown
     * @return a ResponseEntity with HTTP 500 status and a generic error message
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception error) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Unable to generate student report"));
    }
}
