package com.shape.games.weather.infrastructure.factories

import com.shape.games.weather.infrastructure.cache.CacheConfig
import com.shape.games.weather.infrastructure.cache.CacheProvider
import com.shape.games.weather.infrastructure.cache.CacheProviderType
import com.shape.games.weather.infrastructure.cache.CaffeineCacheProvider
import com.shape.games.weather.infrastructure.providers.OpenWeatherMapProvider
import com.shape.games.weather.infrastructure.providers.WeatherProvider
import com.shape.games.weather.infrastructure.providers.WeatherProviderConfig
import com.shape.games.weather.infrastructure.providers.WeatherProviderType
import io.ktor.client.*

/**
 * Factory for creating weather providers
 */
class WeatherProviderFactory {

    fun createProvider(
        config: WeatherProviderConfig,
        httpClient: HttpClient
    ): WeatherProvider {
        return when (config.providerType) {
            WeatherProviderType.OPENWEATHERMAP -> OpenWeatherMapProvider(httpClient, config)
        }
    }
}

/**
 * Factory for creating cache providers
 */
class CacheProviderFactory {

    fun <K : Any, V : Any> createProvider(
        config: CacheConfig
    ): CacheProvider<K, V> {
        return when (config.providerType) {
            CacheProviderType.CAFFEINE -> CaffeineCacheProvider(config)
            CacheProviderType.REDIS -> throw NotImplementedError("Redis provider not implemented yet")
        }
    }
}

