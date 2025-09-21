package com.shape.games

import com.shape.games.weather.infrastructure.config.AppConfig
import com.shape.games.weather.infrastructure.di.DependencyInjection
import com.shape.games.weather.presentation.controllers.WeatherController
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
        weatherApplicationService = dependencyInjection.weatherApplicationService()
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
