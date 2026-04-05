package com.shortify.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class ShortenUrlRequest {

    @NotBlank(message = "URL is required")
    private String url;

    @Min(value = 1, message = "Expiry days must be at least 1")
    @Max(value = 30, message = "Expiry days cannot be more than 30")
    private Integer expiryDays;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getExpiryDays() {
        return expiryDays;
    }

    public void setExpiryDays(Integer expiryDays) {
        this.expiryDays = expiryDays;
    }
}
