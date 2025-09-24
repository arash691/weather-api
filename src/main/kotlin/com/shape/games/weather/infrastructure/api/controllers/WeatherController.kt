package com.shape.games.weather.infrastructure.api.controllers

import com.shape.games.weather.application.WeatherService
import com.shape.games.weather.domain.exceptions.NotFoundException
import com.shape.games.weather.presentation.dto.*
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
        
        val locationsParam = call.request.queryParameters["locations"]
        val temperatureParam = call.request.queryParameters["temperature"]
        val unitParam = call.request.queryParameters["unit"]

        val summaries = weatherService.getWeatherSummaryForFavorites(
            locationsParam, temperatureParam, unitParam
        )

        val response = WeatherSummaryResponse(
            locations = summaries,
            metadata = createResponseMetadata(requestId)
        )

        call.respond(HttpStatusCode.OK, response)
    }

    suspend fun getLocationWeather(call: ApplicationCall) {
        val requestId = generateRequestId()

        call.response.headers.append("X-Request-ID", requestId)

        val locationParam = call.parameters["locationId"]

        val weatherDetails = weatherService.getLocationWeatherDetails(locationParam)
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