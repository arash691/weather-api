package com.shape.games.weather.domain.entities

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.*

class LocationTest {

    @Test
    fun `should create valid location`() {
        val location = Location(
            id = "london",
            name = "London", 
            latitude = 51.5074,
            longitude = -0.1278,
            country = "United Kingdom"
        )
        
        assertEquals("london", location.id)
        assertEquals("London", location.name)
        assertEquals(51.5074, location.latitude)
        assertEquals(-0.1278, location.longitude)
        assertEquals("United Kingdom", location.country)
    }

    @Test
    fun `should reject blank required fields`() {
        assertThrows<IllegalArgumentException> {
            Location("", "London", 51.5074, -0.1278, "UK")
        }
        
        assertThrows<IllegalArgumentException> {
            Location("london", "", 51.5074, -0.1278, "UK")
        }
        
        assertThrows<IllegalArgumentException> {
            Location("london", "London", 51.5074, -0.1278, "")
        }
    }

    @Test
    fun `should reject invalid coordinates`() {
        assertThrows<IllegalArgumentException> {
            Location("test", "Test", 91.0, 0.0, "Country")
        }
        
        assertThrows<IllegalArgumentException> {
            Location("test", "Test", 0.0, 181.0, "Country")
        }
    }

    @Test
    fun `should accept coordinates at boundaries`() {
        val northPole = Location("np", "North Pole", 90.0, 0.0, "Arctic")
        val southPole = Location("sp", "South Pole", -90.0, 0.0, "Antarctica")
        
        assertEquals(90.0, northPole.latitude)
        assertEquals(-90.0, southPole.latitude)
    }
}
