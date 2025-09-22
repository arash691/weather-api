package com.shape.games.weather.infrastructure.config

import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/**
 * Weather API configuration structure supporting multiple providers
 */
data class WeatherConfig(
    val weatherProvider: WeatherProviderConfig,
    val openWeatherMap: OpenWeatherMapConfig,
    val cache: CacheConfig
)

data class WeatherProviderConfig(
    val type: String = "OPENWEATHERMAP", // OPENWEATHERMAP, ACCUWEATHER, WEATHERAPI, etc.
    val fallbackEnabled: Boolean = false,
    val fallbackProvider: String? = null
)

data class OpenWeatherMapConfig(
    val apiKey: String,
    val baseUrl: String = "https://api.openweathermap.org",
    val timeoutMs: Long = 30000
)


data class CacheConfig(
    val weather: CacheTypeConfig,
    val forecast: CacheTypeConfig,
    val location: CacheTypeConfig,
    val maxCacheSize: Long = 1000
)

data class CacheTypeConfig(
    val provider: String = "CAFFEINE", // CAFFEINE, REDIS, HAZELCAST, EHCACHE
    val durationMinutes: Int = 15
)

// Extension properties for backward compatibility
val CacheTypeConfig.weatherCacheDurationMinutes: Duration
    get() = durationMinutes.minutes

val CacheTypeConfig.forecastCacheDurationMinutes: Duration
    get() = durationMinutes.minutes

val CacheTypeConfig.locationCacheDurationMinutes: Duration
    get() = durationMinutes.minutes

