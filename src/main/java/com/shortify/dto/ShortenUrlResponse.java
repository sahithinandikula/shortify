package com.shortify.dto;

import java.time.LocalDateTime;

public class ShortenUrlResponse {

    private final String shortCode;
    private final String shortUrl;
    private final Integer clicks;
    private final LocalDateTime createdAt;
    private final LocalDateTime expiry;

    public ShortenUrlResponse(String shortCode, String shortUrl, Integer clicks, LocalDateTime createdAt, LocalDateTime expiry) {
        this.shortCode = shortCode;
        this.shortUrl = shortUrl;
        this.clicks = clicks;
        this.createdAt = createdAt;
        this.expiry = expiry;
    }

    public String getShortCode() {
        return shortCode;
    }

    public String getShortUrl() {
        return shortUrl;
    }

    public Integer getClicks() {
        return clicks;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getExpiry() {
        return expiry;
    }
}
