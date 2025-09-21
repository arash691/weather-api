package com.shape.games.weather.infrastructure.config

import io.ktor.server.application.*
import org.slf4j.LoggerFactory

/**
 * Application configuration loader
 * Loads and validates configuration from application.yaml
 */
object AppConfig {

    private val logger = LoggerFactory.getLogger(AppConfig::class.java)

    fun load(application: Application): WeatherConfig {
        logger.info("Loading application configuration")

        val config = application.environment.config

        // Load weather provider configuration
        val weatherProviderConfig = WeatherProviderConfig(
            type = config.propertyOrNull("weather.provider.type")?.getString() ?: "OPENWEATHERMAP",
            fallbackEnabled = config.propertyOrNull("weather.provider.fallbackEnabled")?.getString()?.toBoolean()
                ?: false,
            fallbackProvider = config.propertyOrNull("weather.provider.fallbackProvider")?.getString()
        )

        // Load OpenWeatherMap configuration
        val openWeatherMapConfig = OpenWeatherMapConfig(
            apiKey = config.propertyOrNull("openweathermap.apiKey")?.getString()
                ?: throw IllegalStateException("OpenWeatherMap API key is required"),
            baseUrl = config.propertyOrNull("openweathermap.baseUrl")?.getString()
                ?: "https://api.openweathermap.org",
            timeoutMs = config.propertyOrNull("openweathermap.timeoutMs")?.getString()?.toLongOrNull()
                ?: 30000
        )

        // Load rate limit configuration
        val rateLimitConfig = RateLimitConfig(
            algorithm = config.propertyOrNull("rateLimit.algorithm")?.getString() ?: "TOKEN_BUCKET",
            maxRequestsPerDay = config.propertyOrNull("rateLimit.maxRequestsPerDay")?.getString()?.toIntOrNull()
                ?: 10000,
            windowSizeDays = config.propertyOrNull("rateLimit.windowSizeDays")?.getString()?.toIntOrNull()
                ?: 1,
            burstAllowance = config.propertyOrNull("rateLimit.burstAllowance")?.getString()?.toDoubleOrNull()
                ?: 0.2,
            refillRate = config.propertyOrNull("rateLimit.refillRate")?.getString()?.toDoubleOrNull()
        )

        // Load cache configuration
        val cacheConfig = CacheConfig(
            weather = CacheTypeConfig(
                provider = config.propertyOrNull("cache.weather.provider")?.getString() ?: "CAFFEINE",
                durationMinutes = config.propertyOrNull("cache.weather.durationMinutes")?.getString()?.toIntOrNull()
                    ?: 15
            ),
            forecast = CacheTypeConfig(
                provider = config.propertyOrNull("cache.forecast.provider")?.getString() ?: "CAFFEINE",
                durationMinutes = config.propertyOrNull("cache.forecast.durationMinutes")?.getString()?.toIntOrNull()
                    ?: 60
            ),
            location = CacheTypeConfig(
                provider = config.propertyOrNull("cache.location.provider")?.getString() ?: "CAFFEINE",
                durationMinutes = config.propertyOrNull("cache.location.durationMinutes")?.getString()?.toIntOrNull()
                    ?: 1440
            ),
            maxCacheSize = config.propertyOrNull("cache.maxCacheSize")?.getString()?.toLongOrNull()
                ?: 1000
        )

        val weatherConfig = WeatherConfig(
            weatherProvider = weatherProviderConfig,
            openWeatherMap = openWeatherMapConfig,
            rateLimit = rateLimitConfig,
            cache = cacheConfig
        )

        logger.info("Configuration loaded successfully")
        logger.debug("Weather provider: {}", weatherProviderConfig.type)
        logger.debug("OpenWeatherMap base URL: {}", openWeatherMapConfig.baseUrl)
        logger.debug(
            "Rate limit algorithm: {} - {} requests per {} days",
            rateLimitConfig.algorithm, rateLimitConfig.maxRequestsPerDay, rateLimitConfig.windowSizeDays
        )
        logger.debug(
            "Cache providers - Weather: {}, Forecast: {}, Location: {}",
            cacheConfig.weather.provider, cacheConfig.forecast.provider, cacheConfig.location.provider
        )

        return weatherConfig
    }
}
