package com.shape.games.weather.infrastructure.repositories

import com.shape.games.weather.domain.entities.Location
import com.shape.games.weather.domain.entities.WeatherData
import com.shape.games.weather.domain.entities.WeatherForecast
import com.shape.games.weather.domain.exceptions.ServiceUnavailableException
import com.shape.games.weather.domain.repositories.WeatherRepository
import com.shape.games.weather.infrastructure.cache.CacheProvider
import com.shape.games.weather.infrastructure.providers.WeatherProvider
import org.slf4j.LoggerFactory

/**
 * Implementation of WeatherRepository using external weather provider and caching
 */
class WeatherRepositoryImpl(
    private val weatherProvider: WeatherProvider,
    private val weatherCache: CacheProvider<String, WeatherData>,
    private val forecastCache: CacheProvider<String, WeatherForecast>,
    private val locationCache: CacheProvider<String, Location>
) : WeatherRepository {

    private val logger = LoggerFactory.getLogger(WeatherRepositoryImpl::class.java)

    override suspend fun getCurrentWeather(location: Location): WeatherData? {
        val cacheKey = "weather_${location.id}"

        weatherCache.get(cacheKey)?.let { cachedWeather ->
            logger.debug("Found cached weather data for location: {}", location.name)
            return cachedWeather
        }

        return try {
            logger.debug("Fetching weather data from provider for location: {}", location.name)
            val result = weatherProvider.getCurrentWeather(location.latitude, location.longitude)
            val weatherData = result.getOrNull()

            if (weatherData != null) {
                weatherCache.put(cacheKey, weatherData)
                logger.debug("Cached weather data for location: {}", location.name)
            }

            weatherData
        } catch (e: Exception) {
            logger.error("Failed to fetch weather data for location: {}", location.name, e)
            throw ServiceUnavailableException("Unable to fetch weather data for ${location.name}", e)
        }
    }

    override suspend fun getForecast(location: Location, days: Int): WeatherForecast? {
        val cacheKey = "forecast_${location.id}_${days}d"

        forecastCache.get(cacheKey)?.let { cachedForecast ->
            logger.debug("Found cached forecast data for location: {}", location.name)
            return cachedForecast
        }

        return try {
            logger.debug("Fetching forecast data from provider for location: {}", location.name)
            val result = weatherProvider.getForecast(location.latitude, location.longitude, days)
            val forecast = result.getOrNull()

            if (forecast != null) {
                forecastCache.put(cacheKey, forecast)
                logger.debug("Cached forecast data for location: {}", location.name)
            }

            forecast
        } catch (e: Exception) {
            logger.error("Failed to fetch forecast data for location: {}", location.name, e)
            throw ServiceUnavailableException("Unable to fetch forecast data for ${location.name}", e)
        }
    }

    override suspend fun getLocationById(locationId: String): Location? {

        locationCache.get(locationId)?.let { cachedLocation ->
            logger.debug("Found cached location data for ID: {}", locationId)
            return cachedLocation
        }

        val parts = locationId.split(",")
        if (parts.size != 2) {
            logger.warn("Invalid location ID format: {}", locationId)
            return null
        }

        val lat = parts[0].toDoubleOrNull()
        val lon = parts[1].toDoubleOrNull()

        if (lat == null || lon == null) {
            logger.warn("Invalid coordinates in location ID: {}", locationId)
            return null
        }

        return try {
            logger.debug("Fetching location data from provider for coordinates: {},{}", lat, lon)
            val result = weatherProvider.getLocationDetails(lat, lon)
            val location = result.getOrNull()

            if (location != null) {
                locationCache.put(locationId, location)
                logger.debug("Cached location data for ID: {}", locationId)
            }

            location
        } catch (e: Exception) {
            logger.error("Failed to fetch location data for ID: {}", locationId, e)
            throw ServiceUnavailableException("Unable to fetch location data for $locationId", e)
        }
    }

    override suspend fun getLocationsByIds(locationIds: List<String>): List<Location> {
        val locations = mutableListOf<Location>()

        for (locationId in locationIds) {
            try {
                getLocationById(locationId)?.let { location ->
                    locations.add(location)
                }
            } catch (e: Exception) {
                logger.warn("Failed to fetch location for ID: {}", locationId, e)
            }
        }

        return locations
    }
}
