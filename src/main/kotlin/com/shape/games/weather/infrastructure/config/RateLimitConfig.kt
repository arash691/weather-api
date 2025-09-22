package com.shape.games.weather.infrastructure.config

import io.ktor.server.application.*
import io.ktor.server.plugins.ratelimit.*
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

fun Application.configureRateLimit() {

    install(RateLimit) {

        register(RateLimitName("global-external-api")) {
            rateLimiter(limit = 9990, refillPeriod = 24.hours)
            requestKey { call ->
                "openweathermap-global-limit"
            }
        }

        register(RateLimitName("per-user")) {
            rateLimiter(limit = 100, refillPeriod = 1.hours)
            requestKey { call ->
                call.request.local.remoteHost // it's better to use some info from user_token
            }
        }

        register(RateLimitName("weather-burst")) {
            rateLimiter(limit = 20, refillPeriod = 5.minutes)
            requestKey { call ->
                "burst-${call.request.local.remoteHost}" // it's better to use some info from user_token
            }
        }
    }

}
