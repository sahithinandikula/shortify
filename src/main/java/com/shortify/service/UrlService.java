package com.shortify.service;

import com.shortify.dto.AnalyticsResponse;
import com.shortify.dto.ShortenUrlResponse;
import com.shortify.exception.BadRequestException;
import com.shortify.exception.ResourceNotFoundException;
import com.shortify.model.Url;
import com.shortify.repository.UrlRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;

@Service
public class UrlService {

    private static final String BASE62_CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int MIN_SHORT_CODE_LENGTH = 6;
    private static final int DEFAULT_EXPIRY_DAYS = 7;

    private static final Logger logger = LoggerFactory.getLogger(UrlService.class);

    private final UrlRepository urlRepository;

    public UrlService(UrlRepository urlRepository) {
        this.urlRepository = urlRepository;
    }

    public ShortenUrlResponse createShortUrl(String longUrl, Integer expiryDays, String baseUrl) {
        validateUrl(longUrl);

        int requestedExpiryDays = normalizeExpiryDays(expiryDays);
        String normalizedUrl = longUrl.trim();

        Url existingUrl = urlRepository.findByLongUrl(normalizedUrl).orElse(null);
        if (existingUrl != null) {
            if (isExpired(existingUrl)) {
                existingUrl.setExpiryTime(LocalDateTime.now().plusDays(requestedExpiryDays));
                Url refreshedUrl = urlRepository.save(existingUrl);
                logger.info("Existing URL refreshed with shortCode={} longUrl={}", refreshedUrl.getShortCode(), refreshedUrl.getLongUrl());
                return buildShortenResponse(refreshedUrl, baseUrl);
            }

            logger.info("Existing short URL returned for longUrl={} shortCode={}", existingUrl.getLongUrl(), existingUrl.getShortCode());
            return buildShortenResponse(existingUrl, baseUrl);
        }

        Url url = new Url();
        url.setLongUrl(normalizedUrl);
        url.setExpiryTime(LocalDateTime.now().plusDays(requestedExpiryDays));

        Url savedUrl = urlRepository.saveAndFlush(url);
        savedUrl.setShortCode(generateUniqueShortCode(savedUrl.getId()));
        Url updatedUrl = urlRepository.save(savedUrl);

        logger.info("Short URL created shortCode={} longUrl={} expiry={}", updatedUrl.getShortCode(), updatedUrl.getLongUrl(), updatedUrl.getExpiryTime());
        return buildShortenResponse(updatedUrl, baseUrl);
    }

    @Transactional
    public String getLongUrlAndIncrementClicks(String shortCode) {
        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new ResourceNotFoundException("Short URL not found"));

        if (isExpired(url)) {
            logger.warn("Expired short URL accessed shortCode={} longUrl={}", url.getShortCode(), url.getLongUrl());
            throw new ResourceNotFoundException("Short URL has expired");
        }

        url.setClickCount(url.getClickCount() + 1);
        url.setLastAccessedAt(LocalDateTime.now());
        logger.info("Short URL accessed shortCode={} clicks={}", url.getShortCode(), url.getClickCount());
        return url.getLongUrl();
    }

    public AnalyticsResponse getAnalytics(String shortCode) {
        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new ResourceNotFoundException("Short URL not found"));

        return new AnalyticsResponse(
                url.getLongUrl(),
                url.getShortCode(),
                url.getClickCount(),
                url.getCreatedAt(),
                url.getLastAccessedAt(),
                url.getExpiryTime()
        );
    }

    private void validateUrl(String longUrl) {
        if (longUrl == null || longUrl.isBlank()) {
            throw new BadRequestException("URL is required");
        }

        String trimmedUrl = longUrl.trim();
        if (!trimmedUrl.startsWith("http://") && !trimmedUrl.startsWith("https://")) {
            throw new BadRequestException("URL must start with http:// or https://");
        }

        try {
            URI uri = new URI(trimmedUrl);
            if (uri.getHost() == null) {
                throw new BadRequestException("Please provide a valid URL");
            }
        } catch (URISyntaxException exception) {
            throw new BadRequestException("Please provide a valid URL");
        }
    }

    private int normalizeExpiryDays(Integer expiryDays) {
        if (expiryDays == null) {
            return DEFAULT_EXPIRY_DAYS;
        }
        if (expiryDays < 1 || expiryDays > 30) {
            throw new BadRequestException("Expiry days must be between 1 and 30");
        }
        return expiryDays;
    }

    private ShortenUrlResponse buildShortenResponse(Url url, String baseUrl) {
        return new ShortenUrlResponse(
                url.getShortCode(),
                buildShortUrl(baseUrl, url.getShortCode()),
                url.getClickCount(),
                url.getCreatedAt(),
                url.getExpiryTime()
        );
    }

    private String generateUniqueShortCode(Long id) {
        String baseCode = padShortCode(encodeBase62(id));
        String shortCode = baseCode;
        long sequence = 1L;

        while (urlRepository.existsByShortCode(shortCode)) {
            shortCode = padShortCode(baseCode + encodeBase62(sequence));
            sequence++;
        }

        return shortCode;
    }

    private String encodeBase62(Long value) {
        if (value == null || value < 0) {
            throw new BadRequestException("Unable to generate short code");
        }
        if (value == 0) {
            return String.valueOf(BASE62_CHARACTERS.charAt(0));
        }

        StringBuilder encoded = new StringBuilder();
        long currentValue = value;

        while (currentValue > 0) {
            int remainder = (int) (currentValue % 62);
            encoded.append(BASE62_CHARACTERS.charAt(remainder));
            currentValue = currentValue / 62;
        }

        return encoded.reverse().toString();
    }

    private String padShortCode(String encodedValue) {
        if (encodedValue.length() >= MIN_SHORT_CODE_LENGTH) {
            return encodedValue;
        }

        return "a".repeat(MIN_SHORT_CODE_LENGTH - encodedValue.length()) + encodedValue;
    }

    private boolean isExpired(Url url) {
        return url.getExpiryTime() != null && url.getExpiryTime().isBefore(LocalDateTime.now());
    }

    private String buildShortUrl(String baseUrl, String shortCode) {
        return baseUrl.endsWith("/") ? baseUrl + shortCode : baseUrl + "/" + shortCode;
    }
}
