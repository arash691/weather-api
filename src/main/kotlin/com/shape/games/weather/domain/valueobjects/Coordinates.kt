package com.shape.games.weather.domain.valueobjects

import kotlinx.datetime.*

/**
 * Value object representing geographic coordinates
 */
data class Coordinates private constructor(
    val latitude: Double,
    val longitude: Double
) {
    init {
        require(latitude in -90.0..90.0) {
            "Latitude must be between -90 and 90 degrees, got: $latitude"
        }
        require(longitude in -180.0..180.0) {
            "Longitude must be between -180 and 180 degrees, got: $longitude"
        }
    }

    /**
     * Format coordinates as string for external APIs
     */
    fun toCoordinateString(): String = "$latitude,$longitude"

    /**
     * Calculate approximate timezone from longitude
     * Each 15 degrees of longitude represents approximately 1 hour of time difference
     * This is a simplified calculation - real timezones are more complex
     * 
     * @return Approximate timezone for this location
     */
    fun getApproximateTimezone(): TimeZone {
        // Each 15 degrees of longitude represents approximately 1 hour of time difference
        val hoursOffset = (longitude / 15.0).toInt()
        
        // Clamp to reasonable timezone range (-12 to +14)
        val clampedOffset = hoursOffset.coerceIn(-12, 14)
        
        return if (clampedOffset >= 0) {
            TimeZone.of("UTC+$clampedOffset")
        } else {
            TimeZone.of("UTC$clampedOffset")
        }
    }

    /**
     * Get today's date for this location based on its approximate timezone
     * 
     * @return Today's date in this location's approximate timezone
     */
    fun getTodayForLocation(): LocalDate {
        val approximateTimezone = getApproximateTimezone()
        val now = Clock.System.now()
        return now.toLocalDateTime(approximateTimezone).date
    }

    /**
     * Calculate tomorrow's date for this location based on its approximate timezone
     * 
     * @return Tomorrow's date in this location's approximate timezone
     */
    fun getTomorrowForLocation(): LocalDate {
        val approximateTimezone = getApproximateTimezone()
        val now = Clock.System.now()
        val localDateTime = now.toLocalDateTime(approximateTimezone)
        return localDateTime.date.plus(1, DateTimeUnit.DAY)
    }

    /**
     * Check if a date represents "tomorrow" for this location
     * 
     * @param date The date to check
     * @return true if the date is tomorrow for this location
     */
    fun isTomorrow(date: LocalDate): Boolean {
        val tomorrow = getTomorrowForLocation()
        return date == tomorrow
    }

    /**
     * Find the forecast for tomorrow in a list of daily forecasts
     * Uses location-aware date calculation instead of assuming array position
     * 
     * @param forecasts List of daily forecasts
     * @return Tomorrow's forecast or null if not found
     */
    fun findTomorrowForecast(
        forecasts: List<com.shape.games.weather.domain.entities.DailyForecast>
    ): com.shape.games.weather.domain.entities.DailyForecast? {
        val tomorrow = getTomorrowForLocation()
        return forecasts.find { it.date == tomorrow }
    }

    companion object {
        /**
         * Create coordinates from validated doubles
         */
        fun of(latitude: Double, longitude: Double): Coordinates {
            return Coordinates(latitude, longitude)
        }

        /**
         * Parse coordinates from string format "lat,lon"
         */
        fun fromString(coordinateString: String): Result<Coordinates> {
            return try {
                val parts = coordinateString.trim().split(",")
                require(parts.size == 2) {
                    "Coordinate string must contain exactly 2 parts separated by comma"
                }

                val lat = parts[0].trim().toDoubleOrNull()
                    ?: return Result.failure(IllegalArgumentException("Invalid latitude: '${parts[0]}'"))

                val lon = parts[1].trim().toDoubleOrNull()
                    ?: return Result.failure(IllegalArgumentException("Invalid longitude: '${parts[1]}'"))

                Result.success(Coordinates(lat, lon))
            } catch (e: Exception) {
                Result.failure(
                    IllegalArgumentException(
                        "Invalid coordinate format: '$coordinateString'. Expected format: 'lat,lon'",
                        e
                    )
                )
            }
        }

        /**
         * Create multiple coordinates from comma-separated string
         */
        fun fromMultipleString(coordinatesString: String): Result<List<Coordinates>> {
            return try {
                val parts = coordinatesString.split(",").map { it.trim() }.filter { it.isNotEmpty() }

                if (parts.size < 2) {
                    return Result.failure(IllegalArgumentException("At least one coordinate pair required"))
                }

                if (parts.size % 2 != 0) {
                    return Result.failure(IllegalArgumentException("Coordinates must be in pairs (lat,lon)"))
                }

                val coordinates = mutableListOf<Coordinates>()
                for (i in parts.indices step 2) {
                    val lat = parts[i].toDoubleOrNull()
                        ?: return Result.failure(IllegalArgumentException("Invalid latitude: '${parts[i]}'"))
                    val lon = parts[i + 1].toDoubleOrNull()
                        ?: return Result.failure(IllegalArgumentException("Invalid longitude: '${parts[i + 1]}'"))

                    coordinates.add(Coordinates(lat, lon))
                }

                Result.success(coordinates)
            } catch (e: Exception) {
                Result.failure(IllegalArgumentException("Invalid coordinates format: '$coordinatesString'", e))
            }
        }
    }
}
