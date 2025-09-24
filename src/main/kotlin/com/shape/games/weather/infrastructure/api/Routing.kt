package com.shape.games.weather.infrastructure.api

import com.shape.games.weather.infrastructure.api.controllers.WeatherController
import io.ktor.server.application.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.routing.*

fun Application.configureRouting(weatherController: WeatherController) {
    routing {
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
