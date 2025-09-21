package com.shape.games.weather.domain.repositories

import com.shape.games.weather.domain.entities.Location
import com.shape.games.weather.domain.entities.WeatherData
import com.shape.games.weather.domain.entities.WeatherForecast

/**
 * Repository interface for weather data operations
 * Follows the Repository pattern to abstract data access logic
 */
interface WeatherRepository {
    
    /**
     * Get current weather data for a location
     * @param location The location to get weather for
     * @return Current weather data or null if not available
     */
    suspend fun getCurrentWeather(location: Location): WeatherData?
    
    /**
     * Get weather forecast for a location
     * @param location The location to get forecast for
     * @param days Number of days to forecast (max 5)
     * @return Weather forecast data or null if not available
     */
    suspend fun getForecast(location: Location, days: Int = 5): WeatherForecast?
    
    /**
     * Get location information by city ID
     * @param locationId The location ID
     * @return Location information or null if not found
     */
    suspend fun getLocationById(locationId: String): Location?
    
    /**
     * Get multiple locations by their IDs
     * @param locationIds List of location IDs
     * @return List of found locations
     */
    suspend fun getLocationsByIds(locationIds: List<String>): List<Location>
}
