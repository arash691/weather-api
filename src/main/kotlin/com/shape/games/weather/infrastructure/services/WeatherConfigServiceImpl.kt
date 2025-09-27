package com.shape.games.weather.infrastructure.services

import com.shape.games.weather.domain.services.WeatherConfigService
import com.shape.games.weather.infrastructure.config.ApiConfig

/**
 * Infrastructure implementation of WeatherConfigService
 * Adapts infrastructure configuration to domain service interface
 */
class WeatherConfigServiceImpl(
    private val apiConfig: ApiConfig
) : WeatherConfigService {
    
    override fun getDefaultForecastDays(): Int {
        return apiConfig.defaultForecastDays
    }
    
    override fun getDefaultTemperatureUnit(): String {
        return apiConfig.defaultTemperatureUnit
    }
}
