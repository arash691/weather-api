package com.shape.games.weather.presentation.validation

import com.shape.games.weather.domain.entities.TemperatureUnit

/**
 * Validator for weather API requests
 * Implements input validation to ensure request integrity
 */
class WeatherRequestValidator {
    
    /**
     * Validate weather summary request parameters
     */
    fun validateSummaryRequest(
        locationIds: List<String>,
        temperature: Double,
        unit: TemperatureUnit
    ): ValidationResult {
        
        // Check if locations list is not empty
        if (locationIds.isEmpty()) {
            return ValidationResult.invalid("At least one location ID must be provided")
        }
        
        // Check maximum number of locations (prevent abuse)
        if (locationIds.size > 50) {
            return ValidationResult.invalid("Maximum 50 locations allowed per request")
        }
        
        // Check for duplicate location IDs
        if (locationIds.size != locationIds.toSet().size) {
            return ValidationResult.invalid("Duplicate location IDs are not allowed")
        }
        
        // Validate each location ID format
        locationIds.forEach { locationId ->
            val locationValidation = validateLocationId(locationId)
            if (!locationValidation.isValid) {
                return ValidationResult.invalid("Invalid location ID format: $locationId")
            }
        }
        
        // Validate temperature range based on unit
        val tempValidation = validateTemperature(temperature, unit)
        if (!tempValidation.isValid) {
            return tempValidation
        }
        
        return ValidationResult.valid()
    }
    
    /**
     * Validate location ID format
     */
    fun validateLocationId(locationId: String): ValidationResult {
        // Check if location ID is not empty
        if (locationId.isBlank()) {
            return ValidationResult.invalid("Location ID cannot be empty")
        }
        
        // Check location ID format (expecting lat,lon format)
        val parts = locationId.split(",")
        if (parts.size != 2) {
            return ValidationResult.invalid("Location ID must be in format 'latitude,longitude'")
        }
        
        // Validate latitude
        val lat = parts[0].trim().toDoubleOrNull()
        if (lat == null || lat < -90 || lat > 90) {
            return ValidationResult.invalid("Invalid latitude. Must be between -90 and 90")
        }
        
        // Validate longitude
        val lon = parts[1].trim().toDoubleOrNull()
        if (lon == null || lon < -180 || lon > 180) {
            return ValidationResult.invalid("Invalid longitude. Must be between -180 and 180")
        }
        
        return ValidationResult.valid()
    }
    
    /**
     * Validate temperature value based on unit
     */
    fun validateTemperature(temperature: Double, unit: TemperatureUnit): ValidationResult {
        return when (unit) {
            TemperatureUnit.CELSIUS -> {
                if (temperature < -273.15 || temperature > 100) {
                    ValidationResult.invalid("Temperature in Celsius must be between -273.15°C and 100°C")
                } else {
                    ValidationResult.valid()
                }
            }
            TemperatureUnit.FAHRENHEIT -> {
                if (temperature < -459.67 || temperature > 212) {
                    ValidationResult.invalid("Temperature in Fahrenheit must be between -459.67°F and 212°F")
                } else {
                    ValidationResult.valid()
                }
            }
        }
    }
}

/**
 * Result of validation operation
 */
data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null
) {
    companion object {
        fun valid() = ValidationResult(true)
        fun invalid(message: String) = ValidationResult(false, message)
    }
}
