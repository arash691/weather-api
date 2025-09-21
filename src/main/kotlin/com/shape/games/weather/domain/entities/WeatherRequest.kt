package com.shape.games.weather.domain.entities

/**
 * Value object for weather summary request with validation
 */
data class WeatherSummaryRequest(
    val locationIds: List<LocationId>,
    val temperatureThreshold: TemperatureThreshold,
    val unit: TemperatureUnit
) {
    init {
        require(locationIds.isNotEmpty()) { "At least one location must be provided" }
        require(locationIds.size <= 50) { "Maximum 50 locations allowed per request" }
    }

    companion object {
        fun fromQueryParams(
            locationsParam: String,
            temperatureParam: String,
            unitParam: String?
        ): WeatherSummaryRequest {
            // Parse and validate locations
            val locationIds = parseLocationIds(locationsParam)

            // Parse and validate temperature
            val temperature = temperatureParam.toDoubleOrNull()
                ?: throw IllegalArgumentException("Invalid temperature: $temperatureParam")
            val temperatureThreshold = TemperatureThreshold(temperature)

            // Parse and validate unit
            val unit = parseTemperatureUnit(unitParam ?: "celsius")

            return WeatherSummaryRequest(locationIds, temperatureThreshold, unit)
        }

        private fun parseLocationIds(locationsParam: String): List<LocationId> {
            val coordinates = locationsParam.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            require(coordinates.size >= 2) { "At least one location coordinate pair required" }
            require(coordinates.size % 2 == 0) { "Location coordinates must be in pairs (lat,lon)" }

            return coordinates.chunked(2).map { (lat, lon) ->
                LocationId.fromCoordinateString("$lat,$lon")
            }
        }

        private fun parseTemperatureUnit(unit: String): TemperatureUnit {
            return when (unit.lowercase()) {
                "celsius", "c" -> TemperatureUnit.CELSIUS
                "fahrenheit", "f" -> TemperatureUnit.FAHRENHEIT
                else -> throw IllegalArgumentException("Invalid temperature unit: $unit")
            }
        }
    }
}

/**
 * Value object for location ID with validation
 */
data class LocationId(val value: String) {
    init {
        require(value.isNotBlank()) { "Location ID cannot be blank" }
        require(Location.isValidCoordinateString(value)) {
            "Invalid location ID format: $value. Expected 'lat,lon'"
        }
    }

    companion object {
        fun fromCoordinateString(coordinateString: String): LocationId {
            return LocationId(coordinateString)
        }
    }
}

/**
 * Value object for temperature threshold with validation
 */
data class TemperatureThreshold(val value: Double) {
    init {
        require(value >= -100) { "Temperature threshold too low: $value" }
        require(value <= 100) { "Temperature threshold too high: $value" }
    }
}
