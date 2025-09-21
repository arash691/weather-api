package com.shape.games.weather.domain.entities

import kotlinx.datetime.LocalDate

/**
 * Domain entity representing weather forecast for multiple days
 */
data class WeatherForecast(
    val location: Location,
    val forecasts: List<DailyForecast>
)

/**
 * Daily forecast data
 */
data class DailyForecast(
    val date: LocalDate,
    val temperatureMin: Temperature,
    val temperatureMax: Temperature,
    val description: String,
    val humidity: Int,
    val windSpeed: Double,
    val pressure: Int
)
