package com.reliance.grievance.dto;

public class ErrorResponse {
    private String error;
    private String message;

    // Default constructor
    public ErrorResponse() {}

    // Constructor with parameters
    public ErrorResponse(String error, String message) {
        this.error = error;
        this.message = message;
    }

    // Getter and Setter methods
    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}


