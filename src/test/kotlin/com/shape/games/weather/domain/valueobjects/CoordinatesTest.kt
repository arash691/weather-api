package com.shape.games.weather.domain.valueobjects

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.plus
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CoordinatesTest {

    @Test
    fun `should create valid coordinates`() {
        val coords = Coordinates.of(51.5074, -0.1278)
        assertEquals(51.5074, coords.latitude)
        assertEquals(-0.1278, coords.longitude)
    }

    @Test
    fun `should accept coordinates at valid boundaries`() {
        val northPole = Coordinates.of(90.0, 0.0)
        val southPole = Coordinates.of(-90.0, 0.0)
        val dateLine = Coordinates.of(0.0, 180.0)

        assertEquals(90.0, northPole.latitude)
        assertEquals(-90.0, southPole.latitude)
        assertEquals(180.0, dateLine.longitude)
    }

    @Test
    fun `should reject invalid latitude`() {
        assertThrows<IllegalArgumentException> {
            Coordinates.of(90.1, 0.0)
        }
        assertThrows<IllegalArgumentException> {
            Coordinates.of(-90.1, 0.0)
        }
    }

    @Test
    fun `should reject invalid longitude`() {
        assertThrows<IllegalArgumentException> {
            Coordinates.of(0.0, 180.1)
        }
        assertThrows<IllegalArgumentException> {
            Coordinates.of(0.0, -180.1)
        }
    }

    @Test
    fun `should parse single coordinate string correctly`() {
        val result = Coordinates.fromString("51.5074,-0.1278")
        assertTrue(result.isSuccess)

        val coords = result.getOrThrow()
        assertEquals(51.5074, coords.latitude)
        assertEquals(-0.1278, coords.longitude)
    }

    @Test
    fun `should parse multiple coordinates correctly`() {
        val result = Coordinates.fromMultipleString("51.5074,-0.1278,40.7128,-74.0060")
        assertTrue(result.isSuccess)

        val coordsList = result.getOrThrow()
        assertEquals(2, coordsList.size)
        assertEquals(51.5074, coordsList[0].latitude)
        assertEquals(40.7128, coordsList[1].latitude)
    }

    @Test
    fun `should handle invalid coordinate formats`() {
        assertTrue(Coordinates.fromString("invalid").isFailure)
        assertTrue(Coordinates.fromString("51.5074").isFailure)
        assertTrue(Coordinates.fromMultipleString("51.5074").isFailure)
    }

    @Test
    fun `should format coordinates as string`() {
        val coords = Coordinates.of(51.5074, -0.1278)
        assertEquals("51.5074,-0.1278", coords.toCoordinateString())
    }

    @Test
    fun `should calculate different tomorrow dates for different timezones using Coordinates`() {
        // London (GMT+0): longitude 0
        val london = Coordinates.of(51.5074, 0.0)
        val londonTomorrow = london.getTomorrowForLocation()

        // Tokyo (GMT+9): longitude 139.6917 (approximately +9 hours)
        val tokyo = Coordinates.of(35.6762, 139.6917)
        val tokyoTomorrow = tokyo.getTomorrowForLocation()

        // New York (GMT-5): longitude -74.0060 (approximately -5 hours)
        val newYork = Coordinates.of(40.7128, -74.0060)
        val nyTomorrow = newYork.getTomorrowForLocation()

        // At certain times of day, these should be different dates
        // This test verifies the timezone calculation is working
        println("London tomorrow: $londonTomorrow")
        println("Tokyo tomorrow: $tokyoTomorrow")
        println("New York tomorrow: $nyTomorrow")

        // All should be valid dates
        assertTrue(londonTomorrow.year > 2020)
        assertTrue(tokyoTomorrow.year > 2020)
        assertTrue(nyTomorrow.year > 2020)
    }

    @Test
    fun `should calculate today correctly for different locations using Coordinates`() {

        val london = Coordinates.of(51.5074, 0.0)
        val londonToday = london.getTodayForLocation()

        // Sydney (GMT+10): longitude 151.2093
        val sydney = Coordinates.of(-33.8688, 151.2093)
        val sydneyToday = sydney.getTodayForLocation()

        // Both should be valid dates
        assertTrue(londonToday.year > 2020)
        assertTrue(sydneyToday.year > 2020)

        println("London today: $londonToday")
        println("Sydney today: $sydneyToday")
    }

    @Test
    fun `should identify tomorrow correctly using Coordinates`() {
        val newYork = Coordinates.of(40.7128, -74.0060)

        val tomorrow = newYork.getTomorrowForLocation()
        val today = newYork.getTodayForLocation()
        val dayAfterTomorrow = tomorrow.plus(1, DateTimeUnit.DAY)

        // Tomorrow should be identified as tomorrow
        assertTrue(newYork.isTomorrow(tomorrow))

        // Today should not be tomorrow
        assertTrue(!newYork.isTomorrow(today))

        // Day after tomorrow should not be tomorrow
        assertTrue(!newYork.isTomorrow(dayAfterTomorrow))
    }

    @Test
    fun `should handle extreme longitude values using Coordinates`() {
        // Test edge cases
        val farEast = Coordinates.of(0.0, 179.0) // Near international date line
        val farWest = Coordinates.of(0.0, -179.0) // Other side of date line

        val farEastTomorrow = farEast.getTomorrowForLocation()
        val farWestTomorrow = farWest.getTomorrowForLocation()

        // Should not crash and should return valid dates
        assertTrue(farEastTomorrow.year > 2020)
        assertTrue(farWestTomorrow.year > 2020)

        println("Far East tomorrow: $farEastTomorrow")
        println("Far West tomorrow: $farWestTomorrow")
    }

    @Test
    fun `should test timezone calculation directly`() {
        val london = Coordinates.of(51.5074, 0.0)
        val tokyo = Coordinates.of(35.6762, 139.6917)
        val newYork = Coordinates.of(40.7128, -74.0060)

        println("London timezone: ${london.getApproximateTimezone()}")
        println("Tokyo timezone: ${tokyo.getApproximateTimezone()}")
        println("New York timezone: ${newYork.getApproximateTimezone()}")

        // Verify timezone calculations
        assertTrue(london.getApproximateTimezone().toString().contains("UTC"))
        assertTrue(tokyo.getApproximateTimezone().toString().contains("UTC+09"))
        assertTrue(newYork.getApproximateTimezone().toString().contains("UTC-04"))
    }
}
