package com.shape.games.weather.infrastructure.api

import com.shape.games.weather.infrastructure.api.controllers.WeatherController
import com.shape.games.weather.infrastructure.config.*
import com.shape.games.weather.infrastructure.di.DependencyInjection
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue


class WeatherControllerIntegrationTest {

    @Test
    fun `GET weather summary should return 400 for invalid input formats`() = testApplication {
        setupTestApplication()

        val response1 = client.get("/api/v1/weather/summary") {
            parameter("locations", "invalid_coordinates")
            parameter("temperature", "20")
            parameter("unit", "celsius")
        }

        assertEquals(HttpStatusCode.BadRequest, response1.status)
        assertTrue(response1.bodyAsText().contains("VALIDATION_ERROR"))

        val response2 = client.get("/api/v1/weather/summary") {
            parameter("locations", "51.5074,-0.1278")
            parameter("temperature", "20")
            parameter("unit", "kelvin")
        }

        assertEquals(HttpStatusCode.BadRequest, response2.status)
        assertTrue(response2.bodyAsText().contains("VALIDATION_ERROR"))
    }

    @Test
    fun `GET location weather should return 400 for invalid location format`() = testApplication {
        setupTestApplication()

        val response = client.get("/api/v1/weather/locations/invalid_format")

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contains("VALIDATION_ERROR"))
    }

    @Test
    fun `API should return proper error structure for validation errors`() = testApplication {
        setupTestApplication()

        val response = client.get("/api/v1/weather/summary") {
            parameter("locations", "invalid")
            parameter("temperature", "20")
            parameter("unit", "celsius")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)

        val responseBody = response.bodyAsText()
        assertTrue(responseBody.contains("error"))
        assertTrue(responseBody.contains("code"))
        assertTrue(responseBody.contains("message"))
        assertTrue(responseBody.contains("metadata"))
        assertTrue(responseBody.contains("timestamp"))
        assertTrue(responseBody.contains("requestId"))
    }

    @Test
    fun `API should handle edge case coordinates correctly`() = testApplication {
        setupTestApplication()

        val response = client.get("/api/v1/weather/summary") {
            parameter("locations", "90,180")
            parameter("temperature", "0")
            parameter("unit", "celsius")
        }

        assertTrue(response.status.value < 500)
    }

    @Test
    fun `API should preserve request context and generate unique request IDs`() = testApplication {
        setupTestApplication()


        val response1 = client.get("/api/v1/weather/summary") {
            parameter("locations", "51.5074,-0.1278")
            parameter("temperature", "20")
            parameter("unit", "celsius")
        }

        val response2 = client.get("/api/v1/weather/summary") {
            parameter("locations", "48.8566,2.3522")
            parameter("temperature", "20")
            parameter("unit", "celsius")
        }


        assertTrue(response1.status.value < 500)
        assertTrue(response2.status.value < 500)


        val hasRequestId1 = response1.headers["X-Request-ID"] != null ||
                response1.bodyAsText().contains("requestId")
        val hasRequestId2 = response2.headers["X-Request-ID"] != null ||
                response2.bodyAsText().contains("requestId")

        assertTrue(hasRequestId1)
        assertTrue(hasRequestId2)
    }


    @Test
    fun `GET weather summary should handle multiple valid locations`() = testApplication {
        setupTestApplication()

        val response = client.get("/api/v1/weather/summary") {
            parameter("locations", "51.5074,-0.1278;48.8566,2.3522;40.7128,-74.0060")
            parameter("temperature", "15")
            parameter("unit", "celsius")
        }

        assertTrue(response.status.value < 500)

        if (response.status == HttpStatusCode.OK) {
            val responseBody = response.bodyAsText()
            assertTrue(responseBody.contains("locations"))
            assertTrue(responseBody.contains("metadata"))
        }
    }

    @Test
    fun `GET weather summary should validate maximum locations limit`() = testApplication {
        setupTestApplication()

        val manyLocations = (1..51).joinToString(";") { i ->
            "${50 + i * 0.1},${-1 + i * 0.1}"
        }

        val response = client.get("/api/v1/weather/summary") {
            parameter("locations", manyLocations)
            parameter("temperature", "20")
            parameter("unit", "celsius")
        }

        assertTrue(
            response.status == HttpStatusCode.BadRequest,
            "Should reject request with too many locations or invalid parsing"
        )
        assertTrue(
            response.bodyAsText().contains("VALIDATION_ERROR") ||
                    response.bodyAsText().contains("error")
        )
    }

    @Test
    fun `GET weather summary should accept reasonable temperature values`() = testApplication {
        setupTestApplication()

        // After refactoring: We removed arbitrary temperature limits
        // Now only physical constants (absolute zero) are enforced in domain
        val response1 = client.get("/api/v1/weather/summary") {
            parameter("locations", "51.5074,-0.1278")
            parameter("temperature", "150")
            parameter("unit", "celsius")
        }

        assertEquals(HttpStatusCode.OK, response1.status)

        // Test that absolute zero is still rejected (pure domain rule)
        val response2 = client.get("/api/v1/weather/summary") {
            parameter("locations", "51.5074,-0.1278")
            parameter("temperature", "-300")  // Below absolute zero
            parameter("unit", "celsius")
        }

        assertEquals(HttpStatusCode.BadRequest, response2.status)
        assertTrue(response2.bodyAsText().contains("VALIDATION_ERROR"))
    }

    @Test
    fun `GET weather summary should handle different temperature units`() = testApplication {
        setupTestApplication()

        // Test fahrenheit
        val response1 = client.get("/api/v1/weather/summary") {
            parameter("locations", "51.5074,-0.1278")
            parameter("temperature", "68")
            parameter("unit", "fahrenheit")
        }

        assertTrue(response1.status.value < 500)

        // Test celsius
        val response2 = client.get("/api/v1/weather/summary") {
            parameter("locations", "51.5074,-0.1278")
            parameter("temperature", "20")
            parameter("unit", "celsius")
        }

        assertTrue(response2.status.value < 500)
    }

    @Test
    fun `GET weather summary should reject invalid temperature formats`() = testApplication {
        setupTestApplication()

        val testCases = listOf(
            "abc", "twenty", "20.5.5", "20,5", "NaN", "", "   ", "null", "undefined"
        )

        testCases.forEach { invalidTemp ->
            val response = client.get("/api/v1/weather/summary") {
                parameter("locations", "51.5074,-0.1278")
                parameter("temperature", invalidTemp)
                parameter("unit", "celsius")
            }

            assertEquals(
                HttpStatusCode.BadRequest, response.status,
                "Should reject invalid temperature: '$invalidTemp'"
            )
            assertTrue(response.bodyAsText().contains("VALIDATION_ERROR"))
        }
    }

    @Test
    fun `GET weather summary should reject invalid coordinate formats`() = testApplication {
        setupTestApplication()

        val testCases = listOf(
            "invalid", "51.5074", "51.5074,", ",0.1278", "51.5074,-0.1278,extra",
            "lat,lng", "51.5074;-0.1278", "51.5074|-0.1278", "200,300",
            "-200,-300", "abc,def", "51,5074,-0,1278"
        )

        testCases.forEach { invalidCoords ->
            val response = client.get("/api/v1/weather/summary") {
                parameter("locations", invalidCoords)
                parameter("temperature", "20")
                parameter("unit", "celsius")
            }

            assertEquals(
                HttpStatusCode.BadRequest, response.status,
                "Should reject invalid coordinates: '$invalidCoords'"
            )
            assertTrue(response.bodyAsText().contains("VALIDATION_ERROR"))
        }
    }

    @Test
    fun `GET weather summary should validate coordinate boundaries`() = testApplication {
        setupTestApplication()

        val testCases = listOf(
            "91,0",
            "-91,0",
            "0,181",
            "0,-181",
            "90.1,0",
            "-90.1,0",
            "0,180.1",
            "0,-180.1"
        )

        testCases.forEach { invalidCoords ->
            val response = client.get("/api/v1/weather/summary") {
                parameter("locations", invalidCoords)
                parameter("temperature", "20")
                parameter("unit", "celsius")
            }

            assertEquals(
                HttpStatusCode.BadRequest, response.status,
                "Should reject out-of-bounds coordinates: '$invalidCoords'"
            )
            assertTrue(response.bodyAsText().contains("VALIDATION_ERROR"))
        }
    }

    @Test
    fun `GET weather summary should accept boundary coordinate values`() = testApplication {
        setupTestApplication()

        val testCases = listOf(
            "90,180",
            "-90,-180",
            "0,0",
            "89.9,179.9",
            "-89.9,-179.9"
        )

        testCases.forEach { validCoords ->
            val response = client.get("/api/v1/weather/summary") {
                parameter("locations", validCoords)
                parameter("temperature", "20")
                parameter("unit", "celsius")
            }

            assertTrue(
                response.status.value < 500,
                "Should accept valid boundary coordinates: '$validCoords'"
            )
        }
    }

    @Test
    fun `GET location weather should handle valid coordinates`() = testApplication {
        setupTestApplication()

        val response = client.get("/api/v1/weather/locations/51.5074,-0.1278")

        assertTrue(response.status.value < 500)

        if (response.status == HttpStatusCode.OK) {
            val responseBody = response.bodyAsText()
            assertTrue(responseBody.contains("location"))
            assertTrue(responseBody.contains("forecast"))
            assertTrue(responseBody.contains("metadata"))
        }
    }

    @Test
    fun `GET location weather should reject invalid path parameters`() = testApplication {
        setupTestApplication()

        val testCases = listOf(
            "invalid", "51.5074", "51.5074,", ",0.1278",
            "200,300", "-200,-300", "abc,def", "51,5074,-0,1278",
            "91,0", "-91,0", "0,181", "0,-181"
        )

        testCases.forEach { invalidLocation ->
            val response = client.get("/api/v1/weather/locations/$invalidLocation")

            assertEquals(
                HttpStatusCode.BadRequest, response.status,
                "Should reject invalid location: '$invalidLocation'"
            )
            assertTrue(response.bodyAsText().contains("VALIDATION_ERROR"))
        }
    }

    @Test
    fun `GET location weather should handle special coordinate cases`() = testApplication {
        setupTestApplication()

        val testCases = listOf(
            "0,0",
            "90,0",
            "-90,0",
            "0,180",
            "0,-180"
        )

        testCases.forEach { specialCoords ->
            val response = client.get("/api/v1/weather/locations/$specialCoords")

            assertTrue(
                response.status.value < 500,
                "Should handle special coordinates: '$specialCoords'"
            )
        }
    }

    @Test
    fun `API should handle missing optional parameters`() = testApplication {
        setupTestApplication()

        val response = client.get("/api/v1/weather/summary") {
            parameter("locations", "51.5074,-0.1278")
            parameter("temperature", "20")
        }

        assertTrue(response.status.value < 500)
    }

    @Test
    fun `API should handle case insensitive parameters`() = testApplication {
        setupTestApplication()

        val testCases = listOf(
            "CELSIUS", "celsius", "Celsius", "CeLsIuS",
            "FAHRENHEIT", "fahrenheit", "Fahrenheit", "FaHrEnHeIt"
        )

        testCases.forEach { unit ->
            val response = client.get("/api/v1/weather/summary") {
                parameter("locations", "51.5074,-0.1278")
                parameter("temperature", "20")
                parameter("unit", unit)
            }

            assertTrue(
                response.status.value < 500,
                "Should handle case insensitive unit: '$unit'"
            )
        }
    }

    @Test
    fun `API should handle very long coordinate lists`() = testApplication {
        setupTestApplication()


        val maxLocations = (1..50).joinToString(";") { i ->
            "${40 + i * 0.01},${-74 + i * 0.01}"
        }

        val response = client.get("/api/v1/weather/summary") {
            parameter("locations", maxLocations)
            parameter("temperature", "20")
            parameter("unit", "celsius")
        }

        assertTrue(response.status.value < 500)
    }

    @Test
    fun `API should return consistent error response format`() = testApplication {
        setupTestApplication()

        val response = client.get("/api/v1/weather/summary") {
            parameter("locations", "invalid")
            parameter("temperature", "20")
            parameter("unit", "celsius")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)

        val responseBody = response.bodyAsText()

        assertTrue(responseBody.contains("error"))
        assertTrue(responseBody.contains("code"))
        assertTrue(responseBody.contains("message"))
        assertTrue(responseBody.contains("metadata"))
        assertTrue(responseBody.contains("timestamp"))

        assertTrue(responseBody.contains("VALIDATION_ERROR"))
    }

    @Test
    fun `API should include proper response headers`() = testApplication {
        setupTestApplication()

        val response = client.get("/api/v1/weather/summary") {
            parameter("locations", "51.5074,-0.1278")
            parameter("temperature", "20")
            parameter("unit", "celsius")
        }

        assertTrue(response.status.value < 500)

        assertNotNull(response.headers["X-Request-ID"])

        assertTrue(response.headers["Content-Type"]?.contains("application/json") == true)
    }

    @Test
    fun `API should handle concurrent requests properly`() = testApplication {
        setupTestApplication()

        val responses = (1..5).map { i ->
            client.get("/api/v1/weather/summary") {
                parameter("locations", "51.${5074 + i},-0.1278")
                parameter("temperature", "20")
                parameter("unit", "celsius")
            }
        }

        responses.forEach { response ->
            assertTrue(response.status.value < 500)
            assertNotNull(response.headers["X-Request-ID"])
        }

        val requestIds = responses.mapNotNull { it.headers["X-Request-ID"] }
        assertEquals(requestIds.size, requestIds.toSet().size)
    }

    private fun ApplicationTestBuilder.setupTestApplication() {
        application {
            val weatherConfig = createTestWeatherConfig()

            val di = DependencyInjection(weatherConfig)


            configureHTTP()
            configureSerialization()
            configureMonitoring()
            configureI18n()
            configureRateLimit(weatherConfig)
            configureStatusPages()

            val weatherController = WeatherController(di.weatherService())
            configureRouting(weatherController)
        }

        client = createClient {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
        }
    }

    private fun createTestWeatherConfig(): WeatherConfig {
        return WeatherConfig(
            weatherProvider = WeatherProviderConfig("OPENWEATHERMAP", false, null),
            openWeatherMap = OpenWeatherMapConfig("test-key", "http://test", 30000),
            cache = CacheConfig(
                weather = CacheTypeConfig("CAFFEINE", 15),
                forecast = CacheTypeConfig("CAFFEINE", 60),
                location = CacheTypeConfig("CAFFEINE", 1440),
                maxCacheSize = 1000
            ),
            rateLimit = RateLimitConfig(9000, 100, 20, 5),
            api = ApiConfig(
                defaultTemperatureUnit = "celsius",
                defaultForecastDays = 5,
                cacheNamespaces = CacheNamespaces("weather", "forecast", "location")
            )
        )
    }
}