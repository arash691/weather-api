package com.shape.games.weather.domain.valueobjects

import com.shape.games.weather.domain.exceptions.TemperatureValidationException

/**
 * Value object representing a temperature value with unit
 * Encapsulates temperature conversion and validation logic
 */
data class Temperature private constructor(
    val value: Double,
    val unit: TemperatureUnit
) {
    init {
        require(value >= ABSOLUTE_ZERO_CELSIUS) { 
            "Temperature cannot be below absolute zero (-273.15°C), got: $value°${unit.symbol}" 
        }
        require(value <= MAX_REASONABLE_TEMP) { 
            "Temperature seems unreasonably high (>${MAX_REASONABLE_TEMP}°C), got: $value°${unit.symbol}" 
        }
    }

    /**
     * Convert to Celsius
     */
    fun toCelsius(): Double = when (unit) {
        TemperatureUnit.CELSIUS -> value
        TemperatureUnit.FAHRENHEIT -> (value - 32.0) * 5.0 / 9.0
    }

    /**
     * Convert to Fahrenheit
     */
    fun toFahrenheit(): Double = when (unit) {
        TemperatureUnit.CELSIUS -> value * 9.0 / 5.0 + 32.0
        TemperatureUnit.FAHRENHEIT -> value
    }

    /**
     * Convert to specified unit
     */
    fun toUnit(targetUnit: TemperatureUnit): Double = when (targetUnit) {
        TemperatureUnit.CELSIUS -> toCelsius()
        TemperatureUnit.FAHRENHEIT -> toFahrenheit()
    }

    /**
     * Check if this temperature is above the threshold
     */
    fun isAbove(threshold: Temperature): Boolean {
        return toCelsius() > threshold.toCelsius()
    }

    /**
     * Format temperature with unit symbol
     */
    fun format(): String = "$value°${unit.symbol}"

    companion object {
        private const val ABSOLUTE_ZERO_CELSIUS = -273.15
        private const val MAX_REASONABLE_TEMP = 1000.0

        /**
         * Create temperature in Celsius
         */
        fun celsius(value: Double): Temperature = Temperature(value, TemperatureUnit.CELSIUS)

        /**
         * Create temperature in Fahrenheit
         */
        fun fahrenheit(value: Double): Temperature = Temperature(value, TemperatureUnit.FAHRENHEIT)

        /**
         * Parse temperature from string with validation
         */
        fun fromString(temperatureString: String, unit: TemperatureUnit): Result<Temperature> {
            return try {
                val value = temperatureString.trim().toDoubleOrNull()
                    ?: return Result.failure(IllegalArgumentException("Invalid temperature value: '$temperatureString'"))
                
                Result.success(Temperature(value, unit))
            } catch (e: Exception) {
                Result.failure(IllegalArgumentException("Invalid temperature: '$temperatureString'", e))
            }
        }
    }
}

/**
 * Temperature unit enumeration
 */
enum class TemperatureUnit(val symbol: String, val displayName: String) {
    CELSIUS("C", "Celsius"),
    FAHRENHEIT("F", "Fahrenheit");

    companion object {
        /**
         * Parse temperature unit from string with validation
         */
        fun fromString(unitString: String?): Result<TemperatureUnit> {
            return when (unitString?.lowercase()?.trim()) {
                null, "", "celsius", "c" -> Result.success(CELSIUS)
                "fahrenheit", "f" -> Result.success(FAHRENHEIT)
                else -> Result.failure(IllegalArgumentException("Invalid temperature unit: '$unitString'. Supported: celsius, fahrenheit"))
            }
        }
    }
}
