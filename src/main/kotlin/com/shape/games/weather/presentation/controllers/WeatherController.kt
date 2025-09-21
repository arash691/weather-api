package com.shape.games.weather.presentation.controllers

import com.shape.games.NotFoundException
import com.shape.games.weather.application.services.WeatherApplicationService
import com.shape.games.weather.presentation.dto.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import org.slf4j.LoggerFactory

/**
 * Controller handling weather-related HTTP endpoints
 * Focuses only on HTTP concerns - parameter extraction and response formatting
 * Domain validation is handled by domain services (DDD approach)
 * Exception handling is done globally
 */
class WeatherController(
    private val weatherApplicationService: WeatherApplicationService
) {
    
    private val logger = LoggerFactory.getLogger(WeatherController::class.java)
    
    /**
     * Handle GET /api/v1/weather/summary endpoint
     * Pure HTTP concern - extract parameters and delegate to application service
     */
    suspend fun getWeatherSummary(call: ApplicationCall) {
        // Extract HTTP parameters (infrastructure concern)
        val locationsParam = call.request.queryParameters["locations"]
        val temperatureParam = call.request.queryParameters["temperature"]
        val unitParam = call.request.queryParameters["unit"]
        
        // Delegate to application service (domain validation happens there)
        val summaries = weatherApplicationService.getWeatherSummaryForFavorites(
            locationsParam, temperatureParam, unitParam
        )
        
        // Format response (infrastructure concern)
        val response = WeatherSummaryResponse(
            locations = summaries,
            metadata = createResponseMetadata()
        )
        
        call.respond(HttpStatusCode.OK, response)
    }
    
    /**
     * Handle GET /api/v1/weather/locations/{locationId} endpoint
     * Pure HTTP concern - extract parameters and delegate to application service
     */
    suspend fun getLocationWeather(call: ApplicationCall) {
        // Extract HTTP parameter (infrastructure concern)
        val locationParam = call.parameters["locationId"]
        
        // Delegate to application service (domain validation happens there)
        val weatherDetails = weatherApplicationService.getLocationWeatherDetails(locationParam)
            ?: throw NotFoundException("Location not found")
        
        // Format response (infrastructure concern)
        val response = LocationWeatherResponse(
            location = LocationDto(
                id = weatherDetails.location.id,
                name = weatherDetails.location.name,
                country = weatherDetails.location.country,
                latitude = weatherDetails.location.latitude,
                longitude = weatherDetails.location.longitude
            ),
            forecast = weatherDetails.forecast.forecasts.map { forecast ->
                DailyForecastDto(
                    date = forecast.date.toString(),
                    temperatureMin = forecast.temperatureMin.celsius,
                    temperatureMax = forecast.temperatureMax.celsius,
                    temperatureUnit = "celsius",
                    description = forecast.description,
                    humidity = forecast.humidity,
                    windSpeed = forecast.windSpeed,
                    pressure = forecast.pressure
                )
            },
            metadata = createResponseMetadata()
        )
        
        call.respond(HttpStatusCode.OK, response)
    }
    
    private suspend fun createResponseMetadata(): ResponseMetadata {
        return ResponseMetadata(
            timestamp = java.time.Instant.now().toString(),
            rateLimitRemaining = weatherApplicationService.getRemainingRequests()
        )
    }
    
}