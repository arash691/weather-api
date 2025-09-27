package com.shape.games.weather.domain.services

/**
 * Domain service interface for weather configuration
 * Provides configuration values without exposing infrastructure details
 */
interface WeatherConfigService {
    /**
     * Get the default number of forecast days to retrieve
     */
    fun getDefaultForecastDays(): Int
    
    /**
     * Get the default temperature unit for responses
     */
    fun getDefaultTemperatureUnit(): String
}
