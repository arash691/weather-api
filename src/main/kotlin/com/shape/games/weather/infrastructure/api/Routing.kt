package com.shape.games.com.shape.games.weather.infrastructure.api

import com.shape.games.weather.infrastructure.api.controllers.WeatherController
import io.ktor.server.application.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting(weatherController: WeatherController) {
    routing {
        // API info endpoint (no rate limit)
        get("/api/info") {
            call.respond(
                mapOf(
                    "name" to "Weather Integration API",
                    "description" to "Integration layer for weather data with rate limiting and caching",
                    "version" to "v1",
                    "rateLimits" to mapOf(
                        "global" to "9,000 requests/day total (protects 3rd party API)",
                        "perUser" to "100 requests/hour per IP (generous limit for good UX)",
                        "burst" to "20 requests/5 minutes per IP (reasonable burst protection)"
                    ),
                    "endpoints" to listOf(
                        mapOf(
                            "method" to "GET",
                            "path" to "/api/v1/weather/summary",
                            "description" to "Get weather summary for favorite locations above temperature threshold"
                        ),
                        mapOf(
                            "method" to "GET",
                            "path" to "/api/v1/weather/locations/{locationId}",
                            "description" to "Get 5-day weather forecast for a specific location"
                        )
                    )
                )
            )
        }

        // Usage statistics endpoint (helpful for developers)
        get("/api/usage") {
            val clientIP = call.request.local.remoteHost
            call.respond(
                mapOf(
                    "client" to mapOf(
                        "ip" to clientIP,
                        "timestamp" to java.time.Instant.now().toString()
                    ),
                    "rateLimits" to mapOf(
                        "global" to mapOf(
                            "limit" to 9000,
                            "period" to "24 hours",
                            "description" to "Shared across all users to protect 3rd party API"
                        ),
                        "perUser" to mapOf(
                            "limit" to 100,
                            "period" to "1 hour", 
                            "description" to "Per IP address to ensure fair usage"
                        ),
                        "burst" to mapOf(
                            "limit" to 20,
                            "period" to "5 minutes",
                            "description" to "Prevents rapid-fire requests"
                        )
                    ),
                    "tips" to listOf(
                        "Cache responses on your client to reduce API calls",
                        "Space out requests to avoid burst limits",
                        "Check X-RateLimit-* headers to monitor your usage",
                        "Use batch endpoints when possible (e.g., /weather/summary for multiple locations)"
                    )
                )
            )
        }

        // Weather API routes with LAYERED rate limiting
        rateLimit(RateLimitName("global-external-api")) {
            rateLimit(RateLimitName("per-user")) {
                rateLimit(RateLimitName("weather-burst")) {
                    route("/api/v1") {
                        route("/weather") {
                            get("/summary") {
                                weatherController.getWeatherSummary(call)
                            }
                            get("/locations/{locationId}") {
                                weatherController.getLocationWeather(call)
                            }
                        }
                    }
                }
            }
        }
    }
}
