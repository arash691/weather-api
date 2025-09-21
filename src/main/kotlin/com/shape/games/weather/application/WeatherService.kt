package com.shape.games.weather.application

import com.shape.games.weather.domain.entities.Location
import com.shape.games.weather.domain.entities.WeatherForecast
import com.shape.games.weather.domain.exceptions.RateLimitExceededException
import com.shape.games.weather.domain.exceptions.ServiceUnavailableException
import com.shape.games.weather.domain.ratelimit.RateLimitProvider
import com.shape.games.weather.domain.repositories.WeatherRepository
import com.shape.games.weather.domain.services.WeatherRequestValidationService
import com.shape.games.weather.domain.valueobjects.Coordinates
import com.shape.games.weather.domain.valueobjects.Temperature
import com.shape.games.weather.presentation.dto.LocationSummaryDto
import org.slf4j.LoggerFactory

/**
 * Application service for weather operations
 * Orchestrates domain services and handles cross-cutting concerns
 */
class WeatherService(
    private val weatherRepository: WeatherRepository,
    private val rateLimitProvider: RateLimitProvider,
    private val validationService: WeatherRequestValidationService = WeatherRequestValidationService()
) {

    private val logger = LoggerFactory.getLogger(WeatherService::class.java)

    /**
     * Get weather summary for favorite locations where temperature will be above threshold
     * Uses domain validation service for proper DDD validation
     */
    suspend fun getWeatherSummaryForFavorites(
        locationsParam: String?,
        temperatureParam: String?,
        unitParam: String?
    ): List<LocationSummaryDto> {

        // Domain validation
        val validationResult = validationService.validateWeatherSummaryRequest(
            locationsParam, temperatureParam, unitParam
        )

        if (validationResult.isFailure) {
            throw validationResult.exceptionOrNull()!!
        }

        val requestData = validationResult.getOrThrow()

        logger.info(
            "Getting weather summary for {} locations with temp > {}",
            requestData.coordinates.size, requestData.temperatureThreshold.format()
        )

        val summaries = mutableListOf<LocationSummaryDto>()

        for (coordinates in requestData.coordinates) {
            if (!rateLimitProvider.consumeToken()) {
                logger.warn("Rate limit exceeded for location summary request")
                throw RateLimitExceededException("Rate limit exceeded. Please try again later.")
            }

            try {
                val summary = getLocationSummary(coordinates, requestData.temperatureThreshold)
                if (summary != null) {
                    summaries.add(summary)
                }
            } catch (e: Exception) {
                logger.error("Error processing location ${coordinates.toCoordinateString()}", e)
                // Continue with other locations
            }
        }

        return summaries
    }

    /**
     * Get detailed weather forecast for a specific location
     * Uses domain validation service for proper DDD validation
     */
    suspend fun getLocationWeatherDetails(locationParam: String?): LocationWeatherDetails? {

        // Domain validation
        val validationResult = validationService.validateLocationWeatherRequest(locationParam)

        if (validationResult.isFailure) {
            throw validationResult.exceptionOrNull()!!
        }

        val coordinates = validationResult.getOrThrow()

        logger.info("Getting weather details for location: {}", coordinates.toCoordinateString())

        if (!rateLimitProvider.consumeToken()) {
            logger.warn("Rate limit exceeded for location weather request")
            throw RateLimitExceededException("Rate limit exceeded. Please try again later.")
        }

        return try {
            val location = getLocationByCoordinates(coordinates) ?: return null
            val forecast = getForecastForLocation(location) ?: return null

            LocationWeatherDetails(location, forecast)
        } catch (e: Exception) {
            logger.error("Error getting weather details for location ${coordinates.toCoordinateString()}", e)
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
        coordinates: Coordinates,
        temperatureThreshold: Temperature
    ): LocationSummaryDto? {
        val location = getLocationByCoordinates(coordinates) ?: return null
        val forecast = getForecastForLocation(location) ?: return null

        // Get tomorrow's forecast (use first forecast as tomorrow)
        val tomorrow = forecast.forecasts.getOrNull(1)?.date
            ?: forecast.forecasts.firstOrNull()?.date

        val tomorrowForecast = forecast.forecasts.find { it.date == tomorrow }
            ?: return null

        // Convert domain temperature to check threshold
        val maxTempDomain = Temperature.celsius(
            tomorrowForecast.temperatureMax.celsius
        )

        return if (maxTempDomain.isAbove(temperatureThreshold)) {
            LocationSummaryDto(
                locationId = coordinates.toCoordinateString(),
                locationName = location.name,
                country = location.country,
                tomorrowMaxTemperature = maxTempDomain.toUnit(temperatureThreshold.unit),
                temperatureUnit = temperatureThreshold.unit.displayName.lowercase(),
                weatherDescription = tomorrowForecast.description
            )
        } else {
            null
        }
    }

    private suspend fun getLocationByCoordinates(coordinates: Coordinates): Location? {
        val locationId = coordinates.toCoordinateString()
        return weatherRepository.getLocationById(locationId)
    }

    private suspend fun getForecastForLocation(location: Location): WeatherForecast? {
        return weatherRepository.getForecast(location, 5)
    }
}

/**
 * Data class for location weather details
 */
data class LocationWeatherDetails(
    val location: Location,
    val forecast: WeatherForecast
)
