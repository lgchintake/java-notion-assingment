package com.home.practice.exception;

public class StudentApiException extends RuntimeException {

    private final int statusCode;

    /**
     * Constructs a StudentApiException with an HTTP status code and error message.
     *
     * @param statusCode the HTTP status code from the failed API request
     * @param message the error message describing what went wrong
     */
    public StudentApiException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    /**
     * Retrieves the HTTP status code associated with this exception.
     *
     * @return the HTTP status code from the failed API request
     */
    public int getStatusCode() {
        return statusCode;
    }
}
