package com.shape.games.weather.domain.entities

/**
 * Domain entity representing a geographic location with validation
 */
data class Location(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String
) {
    init {
        require(id.isNotBlank()) { "Location ID cannot be blank" }
        require(name.isNotBlank()) { "Location name cannot be blank" }
        require(country.isNotBlank()) { "Country cannot be blank" }
        require(latitude in -90.0..90.0) { "Latitude must be between -90 and 90 degrees" }
        require(longitude in -180.0..180.0) { "Longitude must be between -180 and 180 degrees" }
    }
}
