package com.shape.games.weather.infrastructure.config


/**
 * Weather API configuration structure supporting multiple providers
 */
data class WeatherConfig(
    val weatherProvider: WeatherProviderConfig,
    val openWeatherMap: OpenWeatherMapConfig,
    val cache: CacheConfig,
    val rateLimit: RateLimitConfig,
    val api: ApiConfig
)

data class WeatherProviderConfig(
    val type: String,
    val fallbackEnabled: Boolean,
    val fallbackProvider: String?
)

data class OpenWeatherMapConfig(
    val apiKey: String,
    val baseUrl: String,
    val timeoutMs: Long
)


data class CacheConfig(
    val weather: CacheTypeConfig,
    val forecast: CacheTypeConfig,
    val location: CacheTypeConfig,
    val maxCacheSize: Long
)

data class CacheTypeConfig(
    val provider: String,
    val durationMinutes: Int
)

/**
 * Rate limiting configuration
 */
data class RateLimitConfig(
    val globalDailyLimit: Int,
    val perUserHourlyLimit: Int,
    val burstLimit: Int,
    val burstWindowMinutes: Int
)

/**
 * API configuration for responses and behavior
 */
data class ApiConfig(
    val defaultTemperatureUnit: String,
    val defaultForecastDays: Int,
    val cacheNamespaces: CacheNamespaces
)

/**
 * Cache namespace configuration
 */
data class CacheNamespaces(
    val weather: String,
    val forecast: String,
    val location: String
)



