package com.shape.games.weather.domain.valueobjects

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TemperatureTest {

    @Test
    fun `should create valid temperature objects`() {
        val celsius = Temperature.celsius(25.0)
        assertEquals(25.0, celsius.value)
        assertEquals(TemperatureUnit.CELSIUS, celsius.unit)

        val fahrenheit = Temperature.fahrenheit(77.0)
        assertEquals(77.0, fahrenheit.value)
        assertEquals(TemperatureUnit.FAHRENHEIT, fahrenheit.unit)
    }

    @Test
    fun `should reject temperatures below absolute zero`() {
        assertThrows<IllegalArgumentException> {
            Temperature.celsius(-274.0)
        }
    }

    @Test
    fun `should reject unreasonably high temperatures`() {
        assertThrows<IllegalArgumentException> {
            Temperature.celsius(1001.0)
        }
    }

    @Test
    fun `should convert between celsius and fahrenheit correctly`() {
        val celsius25 = Temperature.celsius(25.0)
        assertEquals(25.0, celsius25.toCelsius(), 0.01)
        assertEquals(77.0, celsius25.toFahrenheit(), 0.01)

        val fahrenheit77 = Temperature.fahrenheit(77.0)
        assertEquals(25.0, fahrenheit77.toCelsius(), 0.01)
        assertEquals(77.0, fahrenheit77.toFahrenheit(), 0.01)
    }

    @Test
    fun `should compare temperatures correctly`() {
        val temp25C = Temperature.celsius(25.0)
        val temp77F = Temperature.fahrenheit(77.0) // Same as 25°C
        val temp30C = Temperature.celsius(30.0)

        assertFalse(temp25C.isAbove(temp77F)) // Equal temperatures
        assertTrue(temp30C.isAbove(temp25C))
        assertFalse(temp25C.isAbove(temp30C))
    }

    @Test
    fun `should parse temperature strings correctly`() {
        val result = Temperature.fromString("25.5", TemperatureUnit.CELSIUS)
        assertTrue(result.isSuccess)
        assertEquals(25.5, result.getOrThrow().value)
    }

    @Test
    fun `should handle invalid temperature strings`() {
        val result = Temperature.fromString("invalid", TemperatureUnit.CELSIUS)
        assertTrue(result.isFailure)
    }
}

class TemperatureUnitTest {

    @Test
    fun `should parse temperature units correctly`() {
        assertEquals(TemperatureUnit.CELSIUS, TemperatureUnit.fromString("celsius").getOrThrow())
        assertEquals(TemperatureUnit.CELSIUS, TemperatureUnit.fromString("c").getOrThrow())
        assertEquals(TemperatureUnit.CELSIUS, TemperatureUnit.fromString(null).getOrThrow())

        assertEquals(TemperatureUnit.FAHRENHEIT, TemperatureUnit.fromString("fahrenheit").getOrThrow())
        assertEquals(TemperatureUnit.FAHRENHEIT, TemperatureUnit.fromString("f").getOrThrow())
    }

    @Test
    fun `should reject invalid temperature units`() {
        assertTrue(TemperatureUnit.fromString("kelvin").isFailure)
        assertTrue(TemperatureUnit.fromString("invalid").isFailure)
    }
}
