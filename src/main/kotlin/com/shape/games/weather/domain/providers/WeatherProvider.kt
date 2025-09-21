package com.shape.games.weather.domain.providers

import com.shape.games.weather.domain.entities.Location
import com.shape.games.weather.domain.entities.WeatherData
import com.shape.games.weather.domain.entities.WeatherForecast
import kotlin.time.Duration

/**
 * Abstract interface for weather data providers
 * Allows switching between different weather APIs (OpenWeatherMap, AccuWeather, etc.)
 */
interface WeatherProvider {
    
    /**
     * Get current weather data for a specific location
     * @param latitude Location latitude
     * @param longitude Location longitude
     * @return Weather data or error
     */
    suspend fun getCurrentWeather(latitude: Double, longitude: Double): WeatherResult<WeatherData>
    
    /**
     * Get weather forecast for a specific location
     * @param latitude Location latitude
     * @param longitude Location longitude
     * @param days Number of forecast days (default: 5)
     * @return Weather forecast or error
     */
    suspend fun getForecast(latitude: Double, longitude: Double, days: Int = 5): WeatherResult<WeatherForecast>
    
    /**
     * Search for locations by name
     * @param query Search query
     * @return List of matching locations or error
     */
    suspend fun searchLocations(query: String): WeatherResult<List<Location>>
    
    /**
     * Get location details by coordinates
     * @param latitude Location latitude
     * @param longitude Location longitude
     * @return Location details or error
     */
    suspend fun getLocationDetails(latitude: Double, longitude: Double): WeatherResult<Location>
    
    /**
     * Check if the provider is available and healthy
     * @return true if healthy, false otherwise
     */
    suspend fun isHealthy(): Boolean
    
    /**
     * Get provider-specific rate limit information
     * @return Rate limit info or null if not available
     */
    suspend fun getRateLimitInfo(): RateLimitInfo?
    
    /**
     * Get provider name
     */
    fun getProviderName(): String
}

/**
 * Generic result type for weather operations
 */
sealed class WeatherResult<out T> {
    data class Success<T>(val data: T) : WeatherResult<T>()
    data class Failure(val error: WeatherProviderError) : WeatherResult<Nothing>()
    
    fun isSuccess(): Boolean = this is Success
    fun isFailure(): Boolean = this is Failure
    
    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Failure -> null
    }
    
    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Failure -> throw RuntimeException(error.message)
    }
}

/**
 * Weather provider error types
 */
sealed class WeatherProviderError(val message: String, val code: String) {
    data class NetworkError(val errorMessage: String) : WeatherProviderError(errorMessage, "NETWORK_ERROR")
    data class RateLimitExceeded(val errorMessage: String) : WeatherProviderError(errorMessage, "RATE_LIMIT_EXCEEDED")
    data class InvalidApiKey(val errorMessage: String) : WeatherProviderError(errorMessage, "INVALID_API_KEY")
    data class LocationNotFound(val errorMessage: String) : WeatherProviderError(errorMessage, "LOCATION_NOT_FOUND")
    data class ProviderUnavailable(val errorMessage: String) : WeatherProviderError(errorMessage, "PROVIDER_UNAVAILABLE")
    data class InvalidRequest(val errorMessage: String) : WeatherProviderError(errorMessage, "INVALID_REQUEST")
    data class UnknownError(val errorMessage: String) : WeatherProviderError(errorMessage, "UNKNOWN_ERROR")
}

/**
 * Rate limit information for a provider
 */
data class RateLimitInfo(
    val maxRequestsPerDay: Int,
    val remainingRequests: Int,
    val resetTime: Long,
    val requestsPerMinute: Int? = null
)

/**
 * Weather provider configuration
 */
data class WeatherProviderConfig(
    val providerType: WeatherProviderType,
    val apiKey: String,
    val baseUrl: String,
    val timeoutMs: Long = 30000,
    val retryAttempts: Int = 3,
    val retryDelay: Duration = Duration.parse("PT1S")
)

/**
 * Supported weather provider types
 */
enum class WeatherProviderType {
    OPENWEATHERMAP,
}
