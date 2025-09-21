package com.shape.games.weather.infrastructure.client.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTOs for OpenWeatherMap API responses
 * These represent the exact structure returned by the 3rd party API
 */

@Serializable
data class OpenWeatherMapCurrentResponse(
    val coord: Coord,
    val weather: List<Weather>,
    val main: Main,
    val wind: Wind,
    val name: String,
    val sys: Sys,
    val dt: Long
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
data class Coord(
    val lat: Double,
    val lon: Double
)

@Serializable
data class Weather(
    val id: Int,
    val main: String,
    val description: String,
    val icon: String
)

@Serializable
data class Main(
    val temp: Double,
    @SerialName("temp_min") val tempMin: Double,
    @SerialName("temp_max") val tempMax: Double,
    val pressure: Int,
    val humidity: Int,
    @SerialName("feels_like") val feelsLike: Double
)

@Serializable
data class Wind(
    val speed: Double,
    val deg: Int? = null
)

@Serializable
data class Sys(
    val country: String,
    val sunrise: Long? = null,
    val sunset: Long? = null
)

@Serializable
data class ForecastItem(
    val dt: Long,
    val main: Main,
    val weather: List<Weather>,
    val wind: Wind,
    @SerialName("dt_txt") val dtTxt: String
)

@Serializable
data class City(
    val name: String,
    val coord: Coord,
    val country: String
)
