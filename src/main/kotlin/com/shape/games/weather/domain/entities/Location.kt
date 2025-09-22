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

    /**
     * Factory method for creating Location from coordinate string
     * @param coordinateString Format: "lat,lon" (e.g., "51.5074,-0.1278")
     */
    companion object {
        fun fromCoordinateString(coordinateString: String): Location {
            val parts = coordinateString.split(",").map { it.trim() }
            require(parts.size == 2) { "Coordinate string must be in format 'lat,lon'" }

            val lat = parts[0].toDoubleOrNull()
                ?: throw IllegalArgumentException("Invalid latitude: ${parts[0]}")
            val lon = parts[1].toDoubleOrNull()
                ?: throw IllegalArgumentException("Invalid longitude: ${parts[1]}")

            return Location(
                id = coordinateString,
                name = "Unknown",
                latitude = lat,
                longitude = lon,
                country = "Unknown"
            )
        }

        fun isValidCoordinateString(coordinateString: String): Boolean {
            return try {
                fromCoordinateString(coordinateString)
                true
            } catch (e: Exception) {
                false
            }
        }
    }
}
