# Weather API

A Kotlin/Ktor weather integration service that wraps OpenWeatherMap API with rate limiting and caching. Built this to avoid hitting API limits while providing a clean interface for weather data.

## What it does

Two main endpoints:
- Get weather summary for multiple locations (only returns places where tomorrow will be warmer than your threshold)
- Get detailed 5-day forecast for a specific location

The API sits between your app and OpenWeatherMap, handling rate limits, caching responses, and providing a cleaner interface.

## Quick Start

```bash
# You need Java 8+ and Maven
mvn clean compile
mvn exec:java
```

Server starts on `http://localhost:8080`

### Get your API key
1. Sign up at [OpenWeatherMap](https://openweathermap.org/api)
2. Update `src/main/resources/application.yaml` with your key

### Try it out

```bash
# Get locations where tomorrow will be > 20°C
curl "http://localhost:8080/api/v1/weather/summary?locations=51.5074,-0.1278,48.8566,2.3522&temperature=20&unit=celsius"

# Get 5-day forecast for London
curl "http://localhost:8080/api/v1/weather/locations/51.5074,-0.1278"

# Check API info
curl "http://localhost:8080/api/info"
```

## Project Structure

### Why this structure?

**Domain layer** = Pure business logic. No Ktor, no HTTP, no external APIs. Just weather rules.

**Application layer** = Coordinates domain objects to fulfill use cases. "Get weather summary" lives here.

**Infrastructure layer** = All the messy external stuff. HTTP clients, caches, configuration files.

This means you can change from OpenWeatherMap to other providers without touching business logic. Or swap Caffeine cache for Redis.

## Configuration

Everything's in `src/main/resources/application.yaml`:

```yaml
# Your OpenWeatherMap settings
openweathermap:
  apiKey: "your-key-here"
  baseUrl: "https://api.openweathermap.org"
  timeoutMs: 30000

# Cache settings (how long to keep data)
cache:
  weather:
    durationMinutes: 15      # Current weather
  forecast:
    durationMinutes: 60      # Forecasts
  location:
    durationMinutes: 1440    # Location data (24 hours)

# Rate limiting (protects your API quota)
rateLimit:
  globalDailyLimit: 9000     # Total requests per day
  perUserHourlyLimit: 100    # Per IP address per hour
  burstLimit: 20             # Rapid requests per 5 minutes
```

## Rate Limiting

Three layers of protection:

1. **Global limit** (9000/day) - Protects your OpenWeatherMap quota
2. **Per-user limit** (100/hour) - Prevents individual abuse
3. **Burst limit** (20/5min) - Stops rapid-fire requests

## Caching Strategy

- **Weather data**: 15 minutes (changes frequently)
- **Forecasts**: 1 hour (more stable)
- **Location info**: 24 hours (rarely changes)

Cache keys include coordinates, so different locations don't interfere. Uses Caffeine for in-memory caching (fast, but doesn't survive restarts).

## Testing

```bash
# Run all tests
mvn test

# Just unit tests
mvn test -Dtest="*Test"

# Just integration tests  
mvn test -Dtest="*IntegrationTest"
```


Built with Ktor 3.3.0, Kotlin 2.2.20, and way too much coffee :)
