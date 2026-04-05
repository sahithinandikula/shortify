package com.shortify.dto;

import java.time.LocalDateTime;

public class ErrorResponse {

    private final LocalDateTime timestamp;
    private final int status;
    private final String error;

    public ErrorResponse(int status, String error) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.error = error;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }
}
