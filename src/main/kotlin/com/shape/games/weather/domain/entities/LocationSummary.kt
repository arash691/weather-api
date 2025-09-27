package com.shape.games.weather.domain.entities

/**
 * Domain entity representing a location weather summary
 * Contains information about a location where temperature will be above threshold tomorrow
 */
data class LocationSummary(
    val locationId: String,
    val locationName: String,
    val country: String,
    val tomorrowMaxTemperature: Double,
    val temperatureUnit: String,
    val weatherDescription: String
)
