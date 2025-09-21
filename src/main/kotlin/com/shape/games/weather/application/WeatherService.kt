package com.shape.games.weather.application.services

import com.shape.games.com.shape.games.weather.infrastructure.config.RateLimitExceededException
import com.shape.games.com.shape.games.weather.infrastructure.config.ServiceUnavailableException
import com.shape.games.weather.domain.cache.CacheProvider
import com.shape.games.weather.domain.entities.*
import com.shape.games.weather.domain.providers.WeatherProvider
import com.shape.games.weather.domain.ratelimit.RateLimitProvider
import com.shape.games.weather.presentation.dto.*
import org.slf4j.LoggerFactory

/**
 * Application service for weather operations
 * Contains business logic for weather-related use cases
 */
class WeatherService(
    private val weatherProvider: WeatherProvider,
    private val rateLimitProvider: RateLimitProvider,
    private val weatherCache: CacheProvider<String, WeatherData>,
    private val forecastCache: CacheProvider<String, WeatherForecast>,
    private val locationCache: CacheProvider<String, Location>
) {
    
    private val logger = LoggerFactory.getLogger(WeatherService::class.java)
    
    /**
     * Get weather summary for favorite locations where temperature will be above threshold
     */
    suspend fun getWeatherSummaryForFavorites(
        request: WeatherSummaryRequest
    ): List<LocationSummaryDto> {
        logger.info("Getting weather summary for {} locations with temp > {}°{}", 
            request.locationIds.size, request.temperatureThreshold.value, request.unit.name.lowercase())
        
        val summaries = mutableListOf<LocationSummaryDto>()
        
        for (locationId in request.locationIds) {
            if (!rateLimitProvider.consumeToken()) {
                logger.warn("Rate limit exceeded for location summary request")
                throw RateLimitExceededException("Rate limit exceeded. Please try again later.")
            }
            
            try {
                val summary = getLocationSummary(locationId, request.temperatureThreshold, request.unit)
                if (summary != null) {
                    summaries.add(summary)
                }
            } catch (e: Exception) {
                logger.error("Error processing location ${locationId.value}", e)
                // Continue with other locations
            }
        }
        
        return summaries
    }
    
    /**
     * Get detailed weather forecast for a specific location
     */
    suspend fun getLocationWeatherDetails(locationId: LocationId): LocationWeatherDetails? {
        logger.info("Getting weather details for location: {}", locationId.value)
        
        if (!rateLimitProvider.consumeToken()) {
            logger.warn("Rate limit exceeded for location weather request")
            throw RateLimitExceededException("Rate limit exceeded. Please try again later.")
        }
        
        return try {
            val location = getLocationById(locationId) ?: return null
            val forecast = getForecastForLocation(location) ?: return null
            
            LocationWeatherDetails(location, forecast)
        } catch (e: Exception) {
            logger.error("Error getting weather details for location ${locationId.value}", e)
            throw ServiceUnavailableException("Unable to fetch weather data. Please try again later.")
        }
    }
    
    /**
     * Get remaining rate limit requests
     */
    suspend fun getRemainingRequests(): Int {
        return rateLimitProvider.getRemainingRequests()
    }
    
    private suspend fun getLocationSummary(
        locationId: LocationId,
        temperatureThreshold: TemperatureThreshold,
        unit: TemperatureUnit
    ): LocationSummaryDto? {
        val location = getLocationById(locationId) ?: return null
        val forecast = getForecastForLocation(location) ?: return null
        
        // Get tomorrow's forecast (use first forecast as tomorrow)
        val tomorrow = forecast.forecasts.getOrNull(1)?.date 
            ?: forecast.forecasts.firstOrNull()?.date
        
        val tomorrowForecast = forecast.forecasts.find { it.date == tomorrow }
            ?: return null
        
        val maxTemp = tomorrowForecast.temperatureMax.inUnit(unit)
        
        return if (maxTemp > temperatureThreshold.value) {
            LocationSummaryDto(
                locationId = location.id,
                locationName = location.name,
                country = location.country,
                tomorrowMaxTemperature = maxTemp,
                temperatureUnit = unit.name.lowercase(),
                weatherDescription = tomorrowForecast.description
            )
        } else {
            null
        }
    }
    
    private suspend fun getLocationById(locationId: LocationId): Location? {
        return locationCache.get(locationId.value) ?: run {
            // Parse coordinates from locationId (format: "lat,lon")
            val parts = locationId.value.split(",")
            if (parts.size == 2) {
                val lat = parts[0].toDoubleOrNull()
                val lon = parts[1].toDoubleOrNull()
                if (lat != null && lon != null) {
                    val result = weatherProvider.getLocationDetails(lat, lon)
                    val location = result.getOrNull()
                    if (location != null) {
                        locationCache.put(locationId.value, location)
                    }
                    location
                } else null
            } else null
        }
    }
    
    private suspend fun getForecastForLocation(location: Location): WeatherForecast? {
        return forecastCache.get(location.id) ?: run {
            val result = weatherProvider.getForecast(location.latitude, location.longitude, 5)
            val forecast = result.getOrNull()
            if (forecast != null) {
                forecastCache.put(location.id, forecast)
            }
            forecast
        }
    }
}

