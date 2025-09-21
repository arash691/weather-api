package com.shape.games.weather.infrastructure.factories

import com.shape.games.weather.domain.cache.CacheConfig
import com.shape.games.weather.domain.cache.CacheProvider
import com.shape.games.weather.domain.cache.CacheProviderType
import com.shape.games.weather.domain.providers.WeatherProvider
import com.shape.games.weather.domain.providers.WeatherProviderConfig
import com.shape.games.weather.domain.providers.WeatherProviderType
import com.shape.games.weather.domain.ratelimit.RateLimitConfig
import com.shape.games.weather.domain.ratelimit.RateLimitProvider
import com.shape.games.weather.domain.ratelimit.RateLimitAlgorithm
import com.shape.games.weather.infrastructure.cache.CaffeineCacheProvider
import com.shape.games.weather.infrastructure.providers.OpenWeatherMapProvider
import com.shape.games.weather.infrastructure.ratelimit.TokenBucketRateLimitProvider
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
            WeatherProviderType.ACCUWEATHER -> throw NotImplementedError("AccuWeather provider not implemented yet")
            WeatherProviderType.WEATHERAPI -> throw NotImplementedError("WeatherAPI provider not implemented yet")
            WeatherProviderType.DARK_SKY -> throw NotImplementedError("Dark Sky provider not implemented yet")
            WeatherProviderType.WEATHERBIT -> throw NotImplementedError("Weatherbit provider not implemented yet")
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

/**
 * Factory for creating rate limit providers
 */
class RateLimitProviderFactory {
    
    fun createProvider(config: RateLimitConfig): RateLimitProvider {
        return when (config.algorithm) {
            RateLimitAlgorithm.TOKEN_BUCKET -> TokenBucketRateLimitProvider(
                maxRequests = config.maxRequests,
                windowSize = config.windowSize,
                burstAllowance = config.burstAllowance
            )
            RateLimitAlgorithm.SLIDING_WINDOW -> throw NotImplementedError("Sliding window provider not implemented yet")
            RateLimitAlgorithm.FIXED_WINDOW -> throw NotImplementedError("Fixed window provider not implemented yet")
            RateLimitAlgorithm.LEAKY_BUCKET -> throw NotImplementedError("Leaky bucket provider not implemented yet")
        }
    }
}
