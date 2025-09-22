package com.shape.games.weather.domain.entities

import kotlinx.datetime.Instant

/**
 * Domain entity representing weather data for a specific location and time
 */
data class WeatherData(
    val location: Location,
    val timestamp: Instant,
    val temperature: Temperature,
    val description: String,
    val humidity: Int,
    val windSpeed: Double,
    val pressure: Int
) {
    init {
        require(description.isNotBlank()) { "Weather description cannot be blank" }
        require(humidity in 0..100) { "Humidity must be between 0 and 100 percent" }
        require(windSpeed >= 0) { "Wind speed cannot be negative" }
        require(pressure > 0) { "Pressure must be positive" }
    }
}

/**
 * Value object for temperature with validation and unit conversion capabilities
 */
data class Temperature(
    val celsius: Double
) {
    init {
        require(celsius >= -273.15) { "Temperature cannot be below absolute zero (-273.15°C)" }
        require(celsius <= 1000) { "Temperature seems unreasonably high (>1000°C)" }
    }

}
