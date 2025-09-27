package com.shape.games.weather.infrastructure.api.controllers

import com.shape.games.com.shape.games.weather.infrastructure.api.dto.DailyForecastDto
import com.shape.games.com.shape.games.weather.infrastructure.api.dto.LocationDto
import com.shape.games.com.shape.games.weather.infrastructure.api.dto.LocationSummaryDto
import com.shape.games.com.shape.games.weather.infrastructure.api.dto.LocationWeatherResponse
import com.shape.games.com.shape.games.weather.infrastructure.api.dto.ResponseMetadata
import com.shape.games.com.shape.games.weather.infrastructure.api.dto.WeatherSummaryResponse
import com.shape.games.weather.application.WeatherService
import com.shape.games.weather.domain.exceptions.NotFoundException
import com.shape.games.weather.infrastructure.api.extensions.getLocationWeatherParams
import com.shape.games.weather.infrastructure.api.extensions.getWeatherSummaryParams
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Controller handling weather-related HTTP endpoints
 */
class WeatherController(
    private val weatherService: WeatherService
) {


    suspend fun getWeatherSummary(call: ApplicationCall) {
        val requestId = generateRequestId()

        call.response.headers.append("X-Request-ID", requestId)

        val params = call.getWeatherSummaryParams()

        val domainSummaries = weatherService.getWeatherSummaryForFavorites(
            params.locations, params.temperature, params.unit
        )

        // Map domain objects to DTOs
        val summaries = domainSummaries.map { domainSummary ->
            LocationSummaryDto(
                locationId = domainSummary.locationId,
                locationName = domainSummary.locationName,
                country = domainSummary.country,
                tomorrowMaxTemperature = domainSummary.tomorrowMaxTemperature,
                temperatureUnit = domainSummary.temperatureUnit,
                weatherDescription = domainSummary.weatherDescription
            )
        }

        val response = WeatherSummaryResponse(
            locations = summaries,
            metadata = createResponseMetadata(requestId)
        )

        call.respond(HttpStatusCode.OK, response)
    }

    suspend fun getLocationWeather(call: ApplicationCall) {
        val requestId = generateRequestId()

        call.response.headers.append("X-Request-ID", requestId)

        val params = call.getLocationWeatherParams()

        val weatherDetails = weatherService.getLocationWeatherDetails(params.locationId)
            ?: throw NotFoundException("Location not found")

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
            metadata = createResponseMetadata(requestId)
        )

        call.respond(HttpStatusCode.OK, response)
    }

    private fun createResponseMetadata(requestId: String? = null): ResponseMetadata {
        return ResponseMetadata(
            timestamp = Instant.now().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT),
            requestId = requestId
        )
    }

    private fun generateRequestId(): String {
        return "req_${UUID.randomUUID().toString().take(8)}"
    }

}