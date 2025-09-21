package com.shape.games.weather.domain.valueobjects

/**
 * Value object representing geographic coordinates
 * Encapsulates validation and business rules for coordinates
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

    companion object {
        /**
         * Create coordinates from validated doubles
         */
        fun of(latitude: Double, longitude: Double): Coordinates {
            return Coordinates(latitude, longitude)
        }

        /**
         * Parse coordinates from string format "lat,lon"
         * Returns Result to handle parsing errors gracefully
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
                Result.failure(IllegalArgumentException("Invalid coordinate format: '$coordinateString'. Expected format: 'lat,lon'", e))
            }
        }

        /**
         * Create multiple coordinates from comma-separated string
         * Format: "lat1,lon1,lat2,lon2,lat3,lon3"
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
