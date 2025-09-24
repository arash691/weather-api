package com.shape.games.weather.domain.services

import com.shape.games.weather.domain.exceptions.ValidationException
import com.shape.games.weather.infrastructure.config.ValidationConfig
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import kotlin.test.*

class WeatherRequestValidationServiceTest {

    private lateinit var validationService: WeatherRequestValidationService
    private lateinit var config: ValidationConfig

    @BeforeEach
    fun setup() {
        config = ValidationConfig(
            maxLocationsPerRequest = 5,
            minTemperatureThreshold = -50.0,
            maxTemperatureThreshold = 60.0,
            maxReasonableTemperature = 1000.0,
            absoluteZeroCelsius = -273.15
        )
        validationService = WeatherRequestValidationService(config)
    }

    @Test
    fun `should validate successful weather summary request`() {
        val result = validationService.validateWeatherSummaryRequest(
            locationsParam = "51.5074,-0.1278",
            temperatureParam = "20",
            unitParam = "celsius"
        )
        
        assertTrue(result.isSuccess)
        val data = result.getOrThrow()
        assertEquals(1, data.coordinates.size)
        assertEquals(20.0, data.temperatureThreshold.value)
    }

    @Test
    fun `should reject null locations parameter`() {
        val result = validationService.validateWeatherSummaryRequest(
            locationsParam = null,
            temperatureParam = "20",
            unitParam = "celsius"
        )
        
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull() as ValidationException
        assertEquals("validation.locations.required", exception.messageKey)
    }

    @Test
    fun `should reject null temperature parameter`() {
        val result = validationService.validateWeatherSummaryRequest(
            locationsParam = "51.5074,-0.1278",
            temperatureParam = null,
            unitParam = "celsius"
        )
        
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull() as ValidationException
        assertEquals("validation.temperature.required", exception.messageKey)
    }

    @Test
    fun `should reject too many locations`() {

        val locations = "1,1,2,2,3,3,4,4,5,5,6,6"
        
        val result = validationService.validateWeatherSummaryRequest(
            locationsParam = locations,
            temperatureParam = "20",
            unitParam = "celsius"
        )
        
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull() as ValidationException
        assertEquals("validation.locations.too_many", exception.messageKey)
    }

    @Test
    fun `should reject temperature outside threshold range`() {

        val resultLow = validationService.validateWeatherSummaryRequest(
            locationsParam = "51.5074,-0.1278",
            temperatureParam = "-60",
            unitParam = "celsius"
        )
        
        assertTrue(resultLow.isFailure)
        val exceptionLow = resultLow.exceptionOrNull() as ValidationException
        assertEquals("validation.temperature.out_of_range", exceptionLow.messageKey)

        val resultHigh = validationService.validateWeatherSummaryRequest(
            locationsParam = "51.5074,-0.1278",
            temperatureParam = "70",
            unitParam = "celsius"
        )
        
        assertTrue(resultHigh.isFailure)
        val exceptionHigh = resultHigh.exceptionOrNull() as ValidationException
        assertEquals("validation.temperature.out_of_range", exceptionHigh.messageKey)
    }

    @Test
    fun `should validate successful location weather request`() {
        val result = validationService.validateLocationWeatherRequest("51.5074,-0.1278")
        
        assertTrue(result.isSuccess)
        val coordinates = result.getOrThrow()
        assertEquals(51.5074, coordinates.latitude)
        assertEquals(-0.1278, coordinates.longitude)
    }

    @Test
    fun `should reject null location parameter`() {
        val result = validationService.validateLocationWeatherRequest(null)
        
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull() as ValidationException
        assertEquals("validation.location.required", exception.messageKey)
    }

    @Test
    fun `should handle different temperature units`() {

        val result = validationService.validateWeatherSummaryRequest(
            locationsParam = "51.5074,-0.1278",
            temperatureParam = "68",
            unitParam = "fahrenheit"
        )
        
        assertTrue(result.isSuccess)
        val data = result.getOrThrow()
        assertEquals(68.0, data.temperatureThreshold.value)
    }

    @Test
    fun `should handle invalid input formats`() {

        val resultCoords = validationService.validateWeatherSummaryRequest(
            locationsParam = "invalid_coordinates",
            temperatureParam = "20",
            unitParam = "celsius"
        )
        assertTrue(resultCoords.isFailure)

        val resultTemp = validationService.validateWeatherSummaryRequest(
            locationsParam = "51.5074,-0.1278",
            temperatureParam = "invalid_temp",
            unitParam = "celsius"
        )
        assertTrue(resultTemp.isFailure)
        

        val resultUnit = validationService.validateWeatherSummaryRequest(
            locationsParam = "51.5074,-0.1278",
            temperatureParam = "20",
            unitParam = "invalid_unit"
        )
        assertTrue(resultUnit.isFailure)
    }
}
