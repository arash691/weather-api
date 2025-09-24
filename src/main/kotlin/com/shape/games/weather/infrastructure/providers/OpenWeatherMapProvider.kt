package com.shape.games.weather.infrastructure.providers

import com.shape.games.weather.domain.entities.*
import com.shape.games.weather.domain.providers.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import org.slf4j.LoggerFactory

/**
 * OpenWeatherMap implementation of WeatherProvider
 * Handles API calls to OpenWeatherMap service
 */
class OpenWeatherMapProvider(
    private val httpClient: HttpClient,
    private val config: WeatherProviderConfig
) : WeatherProvider {

    private val logger = LoggerFactory.getLogger(OpenWeatherMapProvider::class.java)

    override suspend fun getCurrentWeather(latitude: Double, longitude: Double): WeatherResult<WeatherData> {
        return try {
            logger.debug("Fetching current weather for lat: {}, lon: {}", latitude, longitude)

            val response = httpClient.get("${config.baseUrl}/data/2.5/weather") {
                parameter("lat", latitude)
                parameter("lon", longitude)
                parameter("appid", config.apiKey)
                parameter("units", "metric")
            }

            if (response.status.isSuccess()) {
                val weatherResponse = response.body<OpenWeatherMapWeatherResponse>()
                val weatherData = weatherResponse.toWeatherData()
                logger.debug("Successfully fetched current weather for lat: {}, lon: {}", latitude, longitude)
                WeatherResult.Success(weatherData)
            } else {
                val error = when (response.status.value) {
                    401 -> WeatherProviderError.InvalidApiKey("Invalid API key")
                    404 -> WeatherProviderError.LocationNotFound("Location not found")
                    429 -> WeatherProviderError.RateLimitExceeded("Rate limit exceeded")
                    else -> WeatherProviderError.UnknownError("HTTP ${response.status.value}")
                }
                WeatherResult.Failure(error)
            }
        } catch (e: Exception) {
            logger.error("Error fetching current weather for lat: {}, lon: {}", latitude, longitude, e)
            WeatherResult.Failure(WeatherProviderError.NetworkError(e.message ?: "Network error"))
        }
    }

    override suspend fun getForecast(latitude: Double, longitude: Double, days: Int): WeatherResult<WeatherForecast> {
        return try {
            logger.debug("Fetching forecast for lat: {}, lon: {}, days: {}", latitude, longitude, days)

            val response = httpClient.get("${config.baseUrl}/data/2.5/forecast") {
                parameter("lat", latitude)
                parameter("lon", longitude)
                parameter("appid", config.apiKey)
                parameter("units", "metric")
                parameter("cnt", days * 8) // 8 forecasts per day (3-hour intervals)
            }

            if (response.status.isSuccess()) {
                val forecastResponse = response.body<OpenWeatherMapForecastResponse>()
                val forecast = forecastResponse.toWeatherForecast()
                logger.debug("Successfully fetched forecast for lat: {}, lon: {}", latitude, longitude)
                WeatherResult.Success(forecast)
            } else {
                val error = when (response.status.value) {
                    401 -> WeatherProviderError.InvalidApiKey("Invalid API key")
                    404 -> WeatherProviderError.LocationNotFound("Location not found")
                    429 -> WeatherProviderError.RateLimitExceeded("Rate limit exceeded")
                    else -> WeatherProviderError.UnknownError("HTTP ${response.status.value}")
                }
                WeatherResult.Failure(error)
            }
        } catch (e: Exception) {
            logger.error("Error fetching forecast for lat: {}, lon: {}", latitude, longitude, e)
            WeatherResult.Failure(WeatherProviderError.NetworkError(e.message ?: "Network error"))
        }
    }

    override suspend fun searchLocations(query: String): WeatherResult<List<Location>> {
        return try {
            logger.debug("Searching locations for query: {}", query)

            val response = httpClient.get("${config.baseUrl}/geo/1.0/direct") {
                parameter("q", query)
                parameter("limit", 5)
                parameter("appid", config.apiKey)
            }

            if (response.status.isSuccess()) {
                val locations = response.body<List<OpenWeatherMapLocationResponse>>()
                    .map { it.toLocation() }
                logger.debug("Found {} locations for query: {}", locations.size, query)
                WeatherResult.Success(locations)
            } else {
                val error = when (response.status.value) {
                    401 -> WeatherProviderError.InvalidApiKey("Invalid API key")
                    404 -> WeatherProviderError.LocationNotFound("No locations found")
                    429 -> WeatherProviderError.RateLimitExceeded("Rate limit exceeded")
                    else -> WeatherProviderError.UnknownError("HTTP ${response.status.value}")
                }
                WeatherResult.Failure(error)
            }
        } catch (e: Exception) {
            logger.error("Error searching locations for query: {}", query, e)
            WeatherResult.Failure(WeatherProviderError.NetworkError(e.message ?: "Network error"))
        }
    }

    override suspend fun getLocationDetails(latitude: Double, longitude: Double): WeatherResult<Location> {
        return try {
            logger.debug("Getting location details for lat: {}, lon: {}", latitude, longitude)

            val response = httpClient.get("${config.baseUrl}/geo/1.0/reverse") {
                parameter("lat", latitude)
                parameter("lon", longitude)
                parameter("limit", 1)
                parameter("appid", config.apiKey)
            }

            if (response.status.isSuccess()) {
                val locations = response.body<List<OpenWeatherMapLocationResponse>>()
                if (locations.isNotEmpty()) {
                    val location = locations.first().toLocation()
                    logger.debug("Successfully got location details for lat: {}, lon: {}", latitude, longitude)
                    WeatherResult.Success(location)
                } else {
                    WeatherResult.Failure(WeatherProviderError.LocationNotFound("Location not found"))
                }
            } else {
                val error = when (response.status.value) {
                    401 -> WeatherProviderError.InvalidApiKey("Invalid API key")
                    404 -> WeatherProviderError.LocationNotFound("Location not found")
                    429 -> WeatherProviderError.RateLimitExceeded("Rate limit exceeded")
                    else -> WeatherProviderError.UnknownError("HTTP ${response.status.value}")
                }
                WeatherResult.Failure(error)
            }
        } catch (e: Exception) {
            logger.error("Error getting location details for lat: {}, lon: {}", latitude, longitude, e)
            WeatherResult.Failure(WeatherProviderError.NetworkError(e.message ?: "Network error"))
        }
    }

    override suspend fun isHealthy(): Boolean {
        return try {
            val response = httpClient.get("${config.baseUrl}/data/2.5/weather") {
                parameter("lat", 0.0)
                parameter("lon", 0.0)
                parameter("appid", config.apiKey)
            }
            response.status.isSuccess()
        } catch (e: Exception) {
            logger.warn("Health check failed for OpenWeatherMap provider", e)
            false
        }
    }

    override fun getProviderName(): String = "OpenWeatherMap"
}

// OpenWeatherMap API Response DTOs
@Serializable
data class OpenWeatherMapWeatherResponse(
    val coord: Coord,
    val weather: List<Weather>,
    val main: Main,
    val wind: Wind,
    val clouds: Clouds,
    val dt: Long,
    val sys: Sys,
    val name: String
)

@Serializable
data class OpenWeatherMapForecastResponse(
    val list: List<ForecastItem>,
    val city: City
)

@Serializable
data class OpenWeatherMapLocationResponse(
    val name: String,
    val lat: Double,
    val lon: Double,
    val country: String,
    val state: String? = null
)

@Serializable
data class Coord(val lat: Double, val lon: Double)

@Serializable
data class Weather(val id: Int, val main: String, val description: String, val icon: String)

@Serializable
data class Main(
    val temp: Double,
    val feels_like: Double,
    val temp_min: Double,
    val temp_max: Double,
    val pressure: Int,
    val humidity: Int
)

@Serializable
data class Wind(val speed: Double, val deg: Int)

@Serializable
data class Clouds(val all: Int)

@Serializable
data class Sys(val country: String, val sunrise: Long, val sunset: Long)

@Serializable
data class ForecastItem(val dt: Long, val main: Main, val weather: List<Weather>, val wind: Wind, val clouds: Clouds)

@Serializable
data class City(val name: String, val country: String, val coord: Coord)

// Extension functions to convert to domain entities
private fun OpenWeatherMapWeatherResponse.toWeatherData(): WeatherData {
    return WeatherData(
        location = Location(
            id = "${coord.lat},${coord.lon}",
            name = name,
            latitude = coord.lat,
            longitude = coord.lon,
            country = sys.country
        ),
        timestamp = kotlinx.datetime.Instant.fromEpochSeconds(dt),
        temperature = Temperature(main.temp),
        description = weather.firstOrNull()?.description ?: "Unknown",
        humidity = main.humidity,
        windSpeed = wind.speed,
        pressure = main.pressure
    )
}

private fun OpenWeatherMapForecastResponse.toWeatherForecast(): WeatherForecast {
    val dailyForecasts = list.groupBy { it.dt / 86400 } // Group by day
        .values
        .map { dayForecasts ->
            val day = dayForecasts.first()
            val minTemp = dayForecasts.minOf { it.main.temp_min }
            val maxTemp = dayForecasts.maxOf { it.main.temp_max }
            val avgHumidity = dayForecasts.map { it.main.humidity }.average().toInt()
            val avgWindSpeed = dayForecasts.map { it.wind.speed }.average()
            val description = dayForecasts.map { it.weather.firstOrNull()?.description ?: "Unknown" }
                .groupingBy { it }
                .eachCount()
                .maxByOrNull { it.value }?.key ?: "Unknown"

            DailyForecast(
                date = kotlinx.datetime.LocalDate.fromEpochDays((day.dt / 86400).toInt()),
                temperatureMin = Temperature(minTemp),
                temperatureMax = Temperature(maxTemp),
                humidity = avgHumidity,
                windSpeed = avgWindSpeed,
                description = description,
                pressure = day.main.pressure
            )
        }

    return WeatherForecast(
        location = Location(
            id = "${city.coord.lat},${city.coord.lon}",
            name = city.name,
            latitude = city.coord.lat,
            longitude = city.coord.lon,
            country = city.country
        ),
        forecasts = dailyForecasts
    )
}

private fun OpenWeatherMapLocationResponse.toLocation(): Location {
    return Location(
        id = "$lat,$lon",
        name = name,
        latitude = lat,
        longitude = lon,
        country = country
    )
}
