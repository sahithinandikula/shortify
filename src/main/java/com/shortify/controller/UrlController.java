package com.shortify.controller;

import com.shortify.dto.AnalyticsResponse;
import com.shortify.dto.ShortenUrlRequest;
import com.shortify.dto.ShortenUrlResponse;
import com.shortify.exception.ResourceNotFoundException;
import com.shortify.service.UrlService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
public class UrlController {

    private final UrlService urlService;

    public UrlController(UrlService urlService) {
        this.urlService = urlService;
    }

    @PostMapping("/shorten")
    public ResponseEntity<ShortenUrlResponse> shortenUrl(@Valid @RequestBody ShortenUrlRequest request) {
        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        ShortenUrlResponse response = urlService.createShortUrl(request.getUrl(), request.getExpiryDays(), baseUrl);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/analytics/{shortCode}")
    public ResponseEntity<AnalyticsResponse> getAnalytics(@PathVariable String shortCode) {
        return ResponseEntity.ok(urlService.getAnalytics(shortCode));
    }

    @GetMapping("/{shortCode:[a-zA-Z0-9]+}")
    public ResponseEntity<?> redirectToOriginalUrl(@PathVariable String shortCode) {
        try {
            String longUrl = urlService.getLongUrlAndIncrementClicks(shortCode);
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, URI.create(longUrl).toString())
                    .build();
        } catch (ResourceNotFoundException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.TEXT_HTML)
                    .body("""
                            <!DOCTYPE html>
                            <html lang="en">
                            <head>
                                <meta charset="UTF-8">
                                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                                <title>Shortify | Link Unavailable</title>
                                <style>
                                    body { margin: 0; font-family: Georgia, serif; background: #f5efe4; color: #1f2933; display: grid; place-items: center; min-height: 100vh; padding: 24px; }
                                    .card { width: min(100%, 520px); background: #fffdf8; border: 1px solid #d9cbb3; border-radius: 24px; padding: 32px; box-shadow: 0 20px 45px rgba(56, 42, 24, 0.12); text-align: center; }
                                    h1 { margin: 0 0 12px; font-size: 2rem; }
                                    p { margin: 0; color: #52606d; line-height: 1.6; }
                                    a { display: inline-block; margin-top: 18px; color: #0b6e4f; font-weight: 700; }
                                </style>
                            </head>
                            <body>
                                <section class="card">
                                    <h1>Link expired or not found</h1>
                                    <p>The short link you tried to open is no longer available.</p>
                                    <a href="/">Create a new short URL</a>
                                </section>
                            </body>
                            </html>
                            """);
        }
    }
}
