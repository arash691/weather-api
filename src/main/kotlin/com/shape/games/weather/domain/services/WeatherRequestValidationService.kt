package com.shape.games.weather.domain.services

import com.shape.games.weather.domain.exceptions.ValidationException
import com.shape.games.weather.domain.valueobjects.Coordinates
import com.shape.games.weather.domain.valueobjects.Temperature
import com.shape.games.weather.domain.valueobjects.TemperatureUnit
import com.shape.games.weather.infrastructure.config.ValidationConfig

/**
 * Domain service for weather request validation
 * Encapsulates complex business rules and validation logic
 */
class WeatherRequestValidationService(
    private val validationConfig: ValidationConfig
) {

    /**
     * Validate weather summary request parameters
     */
    fun validateWeatherSummaryRequest(
        locationsParam: String?,
        temperatureParam: String?,
        unitParam: String?
    ): Result<WeatherSummaryRequestData> {

        if (locationsParam.isNullOrBlank()) {
            return Result.failure(ValidationException(
                messageKey = "validation.locations.required",
                message = "Locations parameter is required"
            ))
        }

        if (temperatureParam.isNullOrBlank()) {
            return Result.failure(ValidationException(
                messageKey = "validation.temperature.required",
                message = "Temperature parameter is required"
            ))
        }


        val coordinatesResult = Coordinates.fromMultipleString(locationsParam)
        if (coordinatesResult.isFailure) {
            return Result.failure(coordinatesResult.exceptionOrNull()!!)
        }

        val coordinates = coordinatesResult.getOrThrow()

        if (coordinates.size > validationConfig.maxLocationsPerRequest) {
            return Result.failure(ValidationException(
                messageKey = "validation.locations.too_many",
                parameters = arrayOf(validationConfig.maxLocationsPerRequest, coordinates.size),
                message = "Too many locations requested. Maximum allowed: ${validationConfig.maxLocationsPerRequest}, got: ${coordinates.size}"
            ))
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
        if (tempInCelsius < validationConfig.minTemperatureThreshold || tempInCelsius > validationConfig.maxTemperatureThreshold) {
            return Result.failure(ValidationException(
                messageKey = "validation.temperature.out_of_range",
                parameters = arrayOf(validationConfig.minTemperatureThreshold, validationConfig.maxTemperatureThreshold, temperature.format()),
                message = "Temperature threshold out of reasonable range (${validationConfig.minTemperatureThreshold} to ${validationConfig.maxTemperatureThreshold}°C), got: ${temperature.format()}"
            ))
        }

        return Result.success(WeatherSummaryRequestData(coordinates, temperature))
    }

    /**
     * Validate location weather request parameters
     */
    fun validateLocationWeatherRequest(locationParam: String?): Result<Coordinates> {
        if (locationParam.isNullOrBlank()) {
            return Result.failure(ValidationException(
                messageKey = "validation.location.required",
                message = "Location parameter is required"
            ))
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
