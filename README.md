# Weather Integration API

A Kotlin/Ktor-based integration layer for weather data that provides rate-limited access to OpenWeatherMap API with
intelligent caching to optimize API usage.

## Features

### 🚀 Core Functionality

- **Weather Summary**: Get favorite locations where tomorrow's temperature will exceed a threshold
- **Location Forecast**: Get 5-day weather forecast for specific locations
- **Rate Limiting**: Intelligent rate limiting to stay within OpenWeatherMap's 10,000 daily requests
- **Caching**: Multi-layer caching strategy to minimize API calls
- **Error Handling**: Comprehensive error handling and validation

### 🏗️ Architecture

- **Clean Architecture**: Domain, Application, Infrastructure, and Presentation layers
- **SOLID Principles**: Dependency inversion, single responsibility, open/closed principles
- **Repository Pattern**: Abstract data access layer
- **Dependency Injection**: Manual DI container for managing dependencies
- **Validation**: Input validation with detailed error messages

### 🛡️ Quality & Performance

- **Rate Limiting**: Token bucket algorithm with sliding window
- **Caching**: Caffeine-based in-memory caching with configurable TTL
- **Monitoring**: Structured logging and request tracking
- **Testing**: Comprehensive unit and integration tests
- **Configuration**: YAML-based configuration management

## API Endpoints

### GET /weather/summary

Get weather summary for favorite locations where tomorrow's temperature will be above threshold.

**Parameters:**

- `unit` (string): Temperature unit - `celsius` or `fahrenheit`
- `temperature` (number): Minimum temperature threshold
- `locations` (string): Comma-separated location IDs in `latitude,longitude` format

**Example:**

```
GET /weather/summary?unit=celsius&temperature=24&locations=51.5074,-0.1278,48.8566,2.3522
```

### GET /weather/locations/{locationId}

Get 5-day weather forecast for a specific location.

**Parameters:**

- `locationId` (path): Location ID in `latitude,longitude` format

**Example:**

```
GET /weather/locations/51.5074,-0.1278
```

### Additional Endpoints

- `GET /` - Service health and information
- `GET /health` - Health check endpoint
- `GET /api/info` - API documentation

## Configuration

The application is configured via `application.yaml`:

```yaml
app:
  openWeatherMap:
    apiKey: "your-api-key-here"
    baseUrl: "https://api.openweathermap.org/data/2.5"
    timeoutMs: 30000

  cache:
    weatherCacheDurationMinutes: 15
    locationCacheDurationMinutes: 1440  # 24 hours
    maxCacheSize: 1000

  rateLimit:
    maxRequestsPerDay: 9500  # Buffer from 10,000 limit
    windowSizeDays: 1
```

## Running the Application

### Prerequisites

- Java 8 or higher
- Maven 3.6+
- OpenWeatherMap API key

### Build and Run

```bash
# Build the project
mvn clean compile

# Run the application
mvn exec:java

# Or run with custom configuration
mvn exec:java -Dconfig.file=application.yaml
```

### Using Docker (Optional)

```bash
# Build Docker image
docker build -t weather-api .

# Run container
docker run -p 8080:8080 weather-api
```

## Testing

```bash
# Run all tests
mvn test

# Run specific test categories
mvn test -Dtest="*UnitTest"
mvn test -Dtest="*IntegrationTest"
```

## Project Structure

```
src/main/kotlin/com/shape/games/weather/
├── domain/
│   ├── entities/          # Core business entities
│   └── repositories/      # Repository interfaces
├── application/
│   └── services/          # Application services and use cases
├── infrastructure/
│   ├── client/           # External API clients
│   ├── cache/            # Caching implementation
│   ├── config/           # Configuration management
│   ├── di/               # Dependency injection
│   └── repositories/     # Repository implementations
└── presentation/
    ├── controllers/      # HTTP controllers
    ├── dto/              # Data transfer objects
    └── validation/       # Request validation
```

## Design Decisions

### Rate Limiting Strategy

- **Token Bucket with Sliding Window**: Provides smooth rate limiting while preventing burst abuse
- **Buffer Management**: Uses 9,500 requests instead of full 10,000 to provide safety margin
- **Graceful Degradation**: Returns cached data when rate limit is hit

### Caching Strategy

- **Multi-Level Caching**: Separate cache policies for weather data (15 min) vs location data (24 hours)
- **Cache-Aside Pattern**: Application manages cache population and invalidation
- **Memory-Efficient**: Uses Caffeine for high-performance in-memory caching

### Error Handling

- **Structured Errors**: Consistent error response format with error codes
- **Input Validation**: Comprehensive request validation with detailed error messages
- **Circuit Breaker Pattern**: Graceful handling of external API failures

### Testing Strategy

- **Unit Tests**: Test individual components in isolation
- **Integration Tests**: Test complete request/response cycles
- **Test Doubles**: Mock external dependencies for reliable testing

## Performance Considerations

- **Concurrent Safety**: All services are thread-safe using coroutines
- **Resource Management**: Proper cleanup of HTTP clients and caches
- **Memory Usage**: Configurable cache sizes to prevent memory leaks
- **Connection Pooling**: HTTP client connection reuse

## Security Considerations

- **API Key Management**: Secure configuration management
- **Input Validation**: Prevent injection attacks via comprehensive validation
- **Rate Limiting**: Prevent abuse and ensure fair usage
- **Error Information**: Careful error messages to prevent information disclosure

## Monitoring & Observability

- **Structured Logging**: JSON-structured logs with correlation IDs
- **Metrics**: Cache hit rates, rate limit usage, response times
- **Health Checks**: Application and dependency health endpoints
- **Request Tracing**: Complete request lifecycle tracking

## Future Enhancements

- **Persistent Caching**: Redis integration for distributed caching
- **Circuit Breaker**: Hystrix-style circuit breaker for external API calls
- **Metrics Export**: Prometheus metrics integration
- **Authentication**: API key management for client applications
- **Database Integration**: Persistent storage for location mappings
- **Horizontal Scaling**: Load balancer and instance coordination

## License

This project is licensed under the MIT License.