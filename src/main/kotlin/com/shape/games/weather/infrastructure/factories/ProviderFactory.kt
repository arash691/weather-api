package com.shape.games.weather.infrastructure.factories

import com.shape.games.weather.domain.cache.CacheConfig
import com.shape.games.weather.domain.cache.CacheProvider
import com.shape.games.weather.domain.cache.CacheProviderType
import com.shape.games.weather.domain.providers.WeatherProvider
import com.shape.games.weather.domain.providers.WeatherProviderConfig
import com.shape.games.weather.domain.providers.WeatherProviderType
import com.shape.games.weather.infrastructure.cache.CaffeineCacheProvider
import com.shape.games.weather.infrastructure.providers.OpenWeatherMapProvider
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
            CacheProviderType.HAZELCAST -> throw NotImplementedError("Hazelcast provider not implemented yet")
            CacheProviderType.EHCACHE -> throw NotImplementedError("EhCache provider not implemented yet")
            CacheProviderType.MEMORY -> CaffeineCacheProvider(config) // Use Caffeine as memory cache
        }
    }
}

