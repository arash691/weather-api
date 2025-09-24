package com.shape.games.weather.domain.valueobjects

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.*

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
}
