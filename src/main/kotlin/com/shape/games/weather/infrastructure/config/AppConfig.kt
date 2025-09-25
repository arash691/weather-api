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

        val weatherProviderConfig = WeatherProviderConfig(
            type = config.propertyOrNull("weather.provider.type")?.getString()
                ?: throw IllegalStateException("Weather provider type is required"),
            fallbackEnabled = config.propertyOrNull("weather.provider.fallbackEnabled")?.getString()?.toBoolean()
                ?: throw IllegalStateException("Weather provider fallback enabled is required"),
            fallbackProvider = config.propertyOrNull("weather.provider.fallbackProvider")?.getString()
        )

        val openWeatherMapConfig = OpenWeatherMapConfig(
            apiKey = config.propertyOrNull("openweathermap.apiKey")?.getString()
                ?: throw IllegalStateException("OpenWeatherMap API key is required"),
            baseUrl = config.propertyOrNull("openweathermap.baseUrl")?.getString()
                ?: throw IllegalStateException("OpenWeatherMap base URL is required"),
            timeoutMs = config.propertyOrNull("openweathermap.timeoutMs")?.getString()?.toLongOrNull()
                ?: throw IllegalStateException("OpenWeatherMap timeout is required")
        )

        val cacheConfig = CacheConfig(
            weather = CacheTypeConfig(
                provider = config.propertyOrNull("cache.weather.provider")?.getString()
                    ?: throw IllegalStateException("Cache weather provider is required"),
                durationMinutes = config.propertyOrNull("cache.weather.durationMinutes")?.getString()?.toIntOrNull()
                    ?: throw IllegalStateException("Cache weather duration is required")
            ),
            forecast = CacheTypeConfig(
                provider = config.propertyOrNull("cache.forecast.provider")?.getString()
                    ?: throw IllegalStateException("Cache forecast provider is required"),
                durationMinutes = config.propertyOrNull("cache.forecast.durationMinutes")?.getString()?.toIntOrNull()
                    ?: throw IllegalStateException("Cache forecast duration is required")
            ),
            location = CacheTypeConfig(
                provider = config.propertyOrNull("cache.location.provider")?.getString()
                    ?: throw IllegalStateException("Cache location provider is required"),
                durationMinutes = config.propertyOrNull("cache.location.durationMinutes")?.getString()?.toIntOrNull()
                    ?: throw IllegalStateException("Cache location duration is required")
            ),
            maxCacheSize = config.propertyOrNull("cache.maxCacheSize")?.getString()?.toLongOrNull()
                ?: throw IllegalStateException("Cache max size is required")
        )

        val rateLimitConfig = RateLimitConfig(
            globalDailyLimit = config.propertyOrNull("rateLimit.globalDailyLimit")?.getString()?.toIntOrNull()
                ?: throw IllegalStateException("Rate limit global daily limit is required"),
            perUserHourlyLimit = config.propertyOrNull("rateLimit.perUserHourlyLimit")?.getString()?.toIntOrNull()
                ?: throw IllegalStateException("Rate limit per user hourly limit is required"),
            burstLimit = config.propertyOrNull("rateLimit.burstLimit")?.getString()?.toIntOrNull()
                ?: throw IllegalStateException("Rate limit burst limit is required"),
            burstWindowMinutes = config.propertyOrNull("rateLimit.burstWindowMinutes")?.getString()?.toIntOrNull()
                ?: throw IllegalStateException("Rate limit burst window minutes is required")
        )

        val apiConfig = ApiConfig(
            defaultTemperatureUnit = config.propertyOrNull("api.defaultTemperatureUnit")?.getString()
                ?: throw IllegalStateException("API default temperature unit is required"),
            defaultForecastDays = config.propertyOrNull("api.defaultForecastDays")?.getString()?.toIntOrNull()
                ?: throw IllegalStateException("API default forecast days is required"),
            cacheNamespaces = CacheNamespaces(
                weather = config.propertyOrNull("api.cacheNamespaces.weather")?.getString()
                    ?: throw IllegalStateException("API cache namespace for weather is required"),
                forecast = config.propertyOrNull("api.cacheNamespaces.forecast")?.getString()
                    ?: throw IllegalStateException("API cache namespace for forecast is required"),
                location = config.propertyOrNull("api.cacheNamespaces.location")?.getString()
                    ?: throw IllegalStateException("API cache namespace for location is required")
            )
        )

        val weatherConfig = WeatherConfig(
            weatherProvider = weatherProviderConfig,
            openWeatherMap = openWeatherMapConfig,
            cache = cacheConfig,
            rateLimit = rateLimitConfig,
            api = apiConfig
        )

        logger.info("Configuration loaded successfully")
        logger.debug("Weather provider: {}", weatherProviderConfig.type)
        logger.debug("OpenWeatherMap base URL: {}", openWeatherMapConfig.baseUrl)
        logger.debug(
            "Cache providers - Weather: {}, Forecast: {}, Location: {}",
            cacheConfig.weather.provider, cacheConfig.forecast.provider, cacheConfig.location.provider
        )

        return weatherConfig
    }
}
