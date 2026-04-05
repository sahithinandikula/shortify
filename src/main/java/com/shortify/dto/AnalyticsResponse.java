package com.shortify.dto;

import java.time.LocalDateTime;

public class AnalyticsResponse {

    private final String url;
    private final String shortCode;
    private final Integer clicks;
    private final LocalDateTime createdAt;
    private final LocalDateTime lastAccessedAt;
    private final LocalDateTime expiry;

    public AnalyticsResponse(String url, String shortCode, Integer clicks, LocalDateTime createdAt, LocalDateTime lastAccessedAt, LocalDateTime expiry) {
        this.url = url;
        this.shortCode = shortCode;
        this.clicks = clicks;
        this.createdAt = createdAt;
        this.lastAccessedAt = lastAccessedAt;
        this.expiry = expiry;
    }

    public String getUrl() {
        return url;
    }

    public String getShortCode() {
        return shortCode;
    }

    public Integer getClicks() {
        return clicks;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getLastAccessedAt() {
        return lastAccessedAt;
    }

    public LocalDateTime getExpiry() {
        return expiry;
    }
}
