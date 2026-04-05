package com.shortify.repository;

import com.shortify.model.Url;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UrlRepository extends JpaRepository<Url, Long> {

    Optional<Url> findByShortCode(String shortCode);

    Optional<Url> findByLongUrl(String longUrl);

    boolean existsByShortCode(String shortCode);
}
