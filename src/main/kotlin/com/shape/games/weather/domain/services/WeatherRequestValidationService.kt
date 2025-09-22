package com.shape.games.weather.domain.services

import com.shape.games.weather.domain.valueobjects.Coordinates
import com.shape.games.weather.domain.valueobjects.Temperature
import com.shape.games.weather.domain.valueobjects.TemperatureUnit

/**
 * Domain service for weather request validation
 * Encapsulates complex business rules and validation logic
 */
class WeatherRequestValidationService {

    companion object {
        private const val MAX_LOCATIONS_PER_REQUEST = 50
        private const val MIN_TEMPERATURE_THRESHOLD = -100.0
        private const val MAX_TEMPERATURE_THRESHOLD = 100.0
    }

    /**
     * Validate weather summary request parameters
     */
    fun validateWeatherSummaryRequest(
        locationsParam: String?,
        temperatureParam: String?,
        unitParam: String?
    ): Result<WeatherSummaryRequestData> {

        if (locationsParam.isNullOrBlank()) {
            return Result.failure(IllegalArgumentException("Locations parameter is required"))
        }

        if (temperatureParam.isNullOrBlank()) {
            return Result.failure(IllegalArgumentException("Temperature parameter is required"))
        }


        val coordinatesResult = Coordinates.fromMultipleString(locationsParam)
        if (coordinatesResult.isFailure) {
            return Result.failure(coordinatesResult.exceptionOrNull()!!)
        }

        val coordinates = coordinatesResult.getOrThrow()

        if (coordinates.size > MAX_LOCATIONS_PER_REQUEST) {
            return Result.failure(
                IllegalArgumentException(
                    "Too many locations requested. Maximum allowed: $MAX_LOCATIONS_PER_REQUEST, got: ${coordinates.size}"
                )
            )
        }

        val unitResult = TemperatureUnit.fromString(unitParam)
        if (unitResult.isFailure) {
            return Result.failure(unitResult.exceptionOrNull()!!)
        }

        val unit = unitResult.getOrThrow()

        val temperatureResult = Temperature.fromString(temperatureParam, unit)
        if (temperatureResult.isFailure) {
            return Result.failure(temperatureResult.exceptionOrNull()!!)
        }

        val temperature = temperatureResult.getOrThrow()

        val tempInCelsius = temperature.toCelsius()
        if (tempInCelsius < MIN_TEMPERATURE_THRESHOLD || tempInCelsius > MAX_TEMPERATURE_THRESHOLD) {
            return Result.failure(
                IllegalArgumentException(
                    "Temperature threshold out of reasonable range ($MIN_TEMPERATURE_THRESHOLD to $MAX_TEMPERATURE_THRESHOLD°C), got: ${temperature.format()}"
                )
            )
        }

        return Result.success(WeatherSummaryRequestData(coordinates, temperature))
    }

    /**
     * Validate location weather request parameters
     */
    fun validateLocationWeatherRequest(locationParam: String?): Result<Coordinates> {
        if (locationParam.isNullOrBlank()) {
            return Result.failure(IllegalArgumentException("Location parameter is required"))
        }

        return Coordinates.fromString(locationParam)
    }
}

/**
 * Data class representing validated weather summary request
 */
data class WeatherSummaryRequestData(
    val coordinates: List<Coordinates>,
    val temperatureThreshold: Temperature
)
