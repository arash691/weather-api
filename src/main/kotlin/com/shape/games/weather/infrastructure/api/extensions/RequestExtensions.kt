package com.shape.games.weather.infrastructure.api.extensions

import com.shape.games.weather.domain.exceptions.ValidationException
import io.ktor.server.application.*

/**
 * Extension functions for HTTP request parameter validation
 * Moves boilerplate validation from application to infrastructure layer
 */

/**
 * Get required query parameter or throw ValidationException
 */
fun ApplicationCall.getRequiredQueryParam(name: String, messageKey: String = "validation.$name.required"): String {
    val value = request.queryParameters[name]
    if (value.isNullOrBlank()) {
        throw ValidationException(
            messageKey = messageKey,
            message = "$name parameter is required"
        )
    }
    return value
}

/**
 * Get optional query parameter with default value
 */
fun ApplicationCall.getOptionalQueryParam(name: String, defaultValue: String? = null): String? {
    return request.queryParameters[name]?.takeIf { it.isNotBlank() } ?: defaultValue
}

/**
 * Get required path parameter or throw ValidationException
 */
fun ApplicationCall.getRequiredPathParam(name: String, messageKey: String = "validation.$name.required"): String {
    val value = parameters[name]
    if (value.isNullOrBlank()) {
        throw ValidationException(
            messageKey = messageKey,
            message = "$name parameter is required"
        )
    }
    return value
}

/**
 * Validate weather summary request parameters
 */
data class WeatherSummaryParams(
    val locations: String,
    val temperature: String,
    val unit: String?
)

fun ApplicationCall.getWeatherSummaryParams(): WeatherSummaryParams {
    return WeatherSummaryParams(
        locations = getRequiredQueryParam("locations", "validation.locations.required"),
        temperature = getRequiredQueryParam("temperature", "validation.temperature.required"),
        unit = getOptionalQueryParam("unit", "celsius")
    )
}

/**
 * Validate location weather request parameters
 */
data class LocationWeatherParams(
    val locationId: String
)

fun ApplicationCall.getLocationWeatherParams(): LocationWeatherParams {
    return LocationWeatherParams(
        locationId = getRequiredPathParam("locationId", "validation.location.required")
    )
}
