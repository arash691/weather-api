package com.shape.games

import com.shape.games.weather.infrastructure.api.configureRouting
import com.shape.games.weather.infrastructure.api.controllers.WeatherController
import com.shape.games.weather.infrastructure.config.*
import com.shape.games.weather.infrastructure.di.DependencyInjection
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {

    val weatherConfig = AppConfig.load(this)

    val dependencyInjection = DependencyInjection(weatherConfig)

    val weatherController = WeatherController(
        weatherService = dependencyInjection.weatherService()
    )

    configureHTTP()
    configureSerialization()
    configureMonitoring()
    configureI18n()
    configureRateLimit(weatherConfig)
    configureStatusPages()
    configureRouting(weatherController)

    monitor.subscribe(ApplicationStopping) {
        dependencyInjection.cleanup()
    }
}
