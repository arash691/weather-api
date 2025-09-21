package com.shape.games.weather.presentation.dto

import kotlinx.serialization.Serializable

/**
 * DTOs for API responses
 * These define the contract between the API and its clients
 */

@Serializable
data class WeatherSummaryResponse(
    val locations: List<LocationSummaryDto>,
    val metadata: ResponseMetadata
)

@Serializable
data class LocationSummaryDto(
    val locationId: String,
    val locationName: String,
    val country: String,
    val tomorrowMaxTemperature: Double,
    val temperatureUnit: String,
    val weatherDescription: String
)

@Serializable
data class LocationWeatherResponse(
    val location: LocationDto,
    val forecast: List<DailyForecastDto>,
    val metadata: ResponseMetadata
)

@Serializable
data class LocationDto(
    val id: String,
    val name: String,
    val country: String,
    val latitude: Double,
    val longitude: Double
)

@Serializable
data class DailyForecastDto(
    val date: String, // ISO date format (YYYY-MM-DD)
    val temperatureMin: Double,
    val temperatureMax: Double,
    val temperatureUnit: String,
    val description: String,
    val humidity: Int,
    val windSpeed: Double,
    val pressure: Int
)

@Serializable
data class ResponseMetadata(
    val timestamp: String, // ISO datetime format
    val source: String = "weather-integration-api",
    val cacheStatus: String? = null,
    val rateLimitRemaining: Int? = null
)

@Serializable
data class ErrorResponse(
    val error: ErrorDetails,
    val metadata: ResponseMetadata
)

@Serializable
data class ErrorDetails(
    val code: String,
    val message: String,
    val details: String? = null
)
