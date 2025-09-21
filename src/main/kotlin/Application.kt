package com.shape.games

import com.shape.games.com.shape.games.weather.infrastructure.api.configureRouting
import com.shape.games.com.shape.games.weather.infrastructure.config.configureHTTP
import com.shape.games.com.shape.games.weather.infrastructure.config.configureMonitoring
import com.shape.games.com.shape.games.weather.infrastructure.config.configureSerialization
import com.shape.games.com.shape.games.weather.infrastructure.config.configureStatusPages
import com.shape.games.weather.infrastructure.api.controllers.WeatherController
import com.shape.games.weather.infrastructure.config.AppConfig
import com.shape.games.weather.infrastructure.di.DependencyInjection
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    // Load configuration
    val weatherConfig = AppConfig.load(this)

    // Initialize dependency injection
    val dependencyInjection = DependencyInjection(weatherConfig)

    // Create weather controller with application service
    val weatherController = WeatherController(
        weatherService = dependencyInjection.weatherService()
    )

    // Configure Ktor plugins
    configureHTTP()
    configureSerialization()
    configureMonitoring()
    configureStatusPages()

    // Configure routing
    configureRouting(weatherController)

    // Register shutdown hook for cleanup  
    monitor.subscribe(ApplicationStopping) {
        dependencyInjection.cleanup()
    }
}
