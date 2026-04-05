# Shortify

Shortify is a beginner-friendly URL shortener built with Java 17+, Spring Boot, Spring Data JPA, Maven, H2 in-memory database, and a simple HTML/CSS frontend.

## Features

- Base62 short code generation using the database ID
- Redirect to the original URL with `GET /{shortCode}`
- Click tracking with `createdAt` and `lastAccessedAt`
- Link expiry support with automatic redirect blocking after expiry
- URL input validation for `http://` and `https://`
- Structured JSON API responses
- Simple browser UI at `http://localhost:8080`

## API Endpoints

### `POST /shorten`

Request:

```json
{
  "url": "https://example.com"
}
```

Response:

```json
{
  "shortCode": "b",
  "shortUrl": "http://localhost:8080/b",
  "clicks": 0,
  "createdAt": "2026-04-04T15:00:00",
  "expiry": "2026-04-11T15:00:00"
}
```

### `GET /{shortCode}`

- Redirects with HTTP `302`
- Returns `404` if the short code does not exist or has expired

### `GET /analytics/{shortCode}`

Response:

```json
{
  "url": "https://example.com",
  "shortCode": "b",
  "clicks": 3,
  "createdAt": "2026-04-04T15:00:00",
  "lastAccessedAt": "2026-04-04T15:05:00",
  "expiry": "2026-04-11T15:00:00"
}
```

## How To Run

The project uses H2 in-memory database by default, so no PostgreSQL setup is needed.

If Maven says `JAVA_HOME` is not defined correctly, set it first:

```powershell
$env:JAVA_HOME="C:\Program Files\Java\jdk-20"
```

Run the app:

```powershell
mvn spring-boot:run
```

If `mvn` is not on your PATH:

```powershell
& "C:\Users\Sahit\.maven\maven-3.9.14(1)\bin\mvn.cmd" spring-boot:run
```

The app starts on `http://localhost:8080` and the H2 console is available at `http://localhost:8080/h2-console`.

## Architecture

```text
src/main/java/com/shortify
|- controller   -> handles HTTP requests and redirects
|- dto          -> request and response payloads
|- exception    -> error handling
|- model        -> JPA entity mapped to the urls table
|- repository   -> database access with Spring Data JPA
\- service      -> URL validation, Base62 generation, expiry, and analytics logic
```
