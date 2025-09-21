package com.shape.games

import com.shape.games.weather.presentation.controllers.WeatherController
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting(weatherController: WeatherController) {
    routing {

        // Weather API routes with simple versioning
        route("/api/v1") {
            route("/weather") {
                // GET /api/v1/weather/summary?unit=celsius&temperature=24&locations=2345,1456,7653
                get("/summary") {
                    weatherController.getWeatherSummary(call)
                }

                // GET /api/v1/weather/locations/{locationId}
                get("/locations/{locationId}") {
                    weatherController.getLocationWeather(call)
                }
            }
        }
    }
}
