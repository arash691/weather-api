package com.shape.games.weather.infrastructure.di

import com.shape.games.weather.application.WeatherService
import com.shape.games.weather.domain.repositories.WeatherRepository
import com.shape.games.weather.infrastructure.repositories.WeatherRepositoryImpl
import com.shape.games.weather.domain.cache.CacheConfig
import com.shape.games.weather.domain.cache.CacheProvider
import com.shape.games.weather.domain.cache.CacheProviderType
import com.shape.games.weather.domain.entities.Location
import com.shape.games.weather.domain.entities.WeatherData
import com.shape.games.weather.domain.entities.WeatherForecast
import com.shape.games.weather.domain.providers.WeatherProvider
import com.shape.games.weather.domain.providers.WeatherProviderConfig
import com.shape.games.weather.domain.providers.WeatherProviderType
import com.shape.games.weather.domain.ratelimit.RateLimitConfig
import com.shape.games.weather.domain.ratelimit.RateLimitProvider
import com.shape.games.weather.domain.ratelimit.RateLimitAlgorithm
import com.shape.games.weather.infrastructure.factories.CacheProviderFactory
import com.shape.games.weather.infrastructure.factories.RateLimitProviderFactory
import com.shape.games.weather.infrastructure.factories.WeatherProviderFactory
import com.shape.games.weather.infrastructure.config.WeatherConfig
import com.shape.games.weather.infrastructure.config.windowSizeDuration
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import kotlin.time.Duration.Companion.minutes

/**
 * Dependency injection with provider abstractions
 * Supports switching between different providers via configuration
 */
class DependencyInjection(private val config: WeatherConfig) {
    
    private val logger = LoggerFactory.getLogger(DependencyInjection::class.java)
    

    private val weatherProviderFactory = WeatherProviderFactory()
    private val cacheProviderFactory = CacheProviderFactory()
    private val rateLimitProviderFactory = RateLimitProviderFactory()
    

    private val httpClient: HttpClient by lazy {
        logger.info("Initializing HTTP client")
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    prettyPrint = true
                })
            }
            
            install(HttpTimeout) {
                requestTimeoutMillis = config.openWeatherMap.timeoutMs
                connectTimeoutMillis = config.openWeatherMap.timeoutMs
                socketTimeoutMillis = config.openWeatherMap.timeoutMs
            }
            
            if (logger.isDebugEnabled) {
                install(Logging) {
                    logger = Logger.DEFAULT
                    level = LogLevel.INFO
                }
            }
            
            defaultRequest {
                headers.append("User-Agent", "WeatherIntegrationAPI/1.0")
            }
        }
    }

    private val weatherProvider: WeatherProvider by lazy {
        logger.info("Initializing weather provider: {}", config.weatherProvider.type)
        val providerConfig = WeatherProviderConfig(
            providerType = WeatherProviderType.valueOf(config.weatherProvider.type.uppercase()),
            apiKey = config.openWeatherMap.apiKey,
            baseUrl = config.openWeatherMap.baseUrl,
            timeoutMs = config.openWeatherMap.timeoutMs
        )
        weatherProviderFactory.createProvider(providerConfig, httpClient)
    }
    

    private val rateLimitProvider: RateLimitProvider by lazy {
        logger.info("Initializing rate limit provider: {}", config.rateLimit.algorithm)
        val rateLimitConfig = RateLimitConfig(
            maxRequests = config.rateLimit.maxRequestsPerDay,
            windowSize = config.rateLimit.windowSizeDuration,
            algorithm = RateLimitAlgorithm.valueOf(config.rateLimit.algorithm.uppercase()),
            burstAllowance = config.rateLimit.burstAllowance
        )
        rateLimitProviderFactory.createProvider(rateLimitConfig)
    }
    

    private val weatherCache: CacheProvider<String, WeatherData> by lazy {
        logger.info("Initializing weather cache provider: {}", config.cache.weather.provider)
        val cacheConfig = CacheConfig(
            providerType = CacheProviderType.valueOf(config.cache.weather.provider.uppercase()),
            maxSize = config.cache.maxCacheSize,
            expireAfterWrite = config.cache.weather.durationMinutes.minutes,
            namespace = "weather"
        )
        cacheProviderFactory.createProvider(cacheConfig)
    }
    
    private val forecastCache: CacheProvider<String, WeatherForecast> by lazy {
        logger.info("Initializing forecast cache provider: {}", config.cache.forecast.provider)
        val cacheConfig = CacheConfig(
            providerType = CacheProviderType.valueOf(config.cache.forecast.provider.uppercase()),
            maxSize = config.cache.maxCacheSize,
            expireAfterWrite = config.cache.forecast.durationMinutes.minutes,
            namespace = "forecast"
        )
        cacheProviderFactory.createProvider(cacheConfig)
    }
    
    private val locationCache: CacheProvider<String, Location> by lazy {
        logger.info("Initializing location cache provider: {}", config.cache.location.provider)
        val cacheConfig = CacheConfig(
            providerType = CacheProviderType.valueOf(config.cache.location.provider.uppercase()),
            maxSize = config.cache.maxCacheSize,
            expireAfterWrite = config.cache.location.durationMinutes.minutes,
            namespace = "location"
        )
        cacheProviderFactory.createProvider(cacheConfig)
    }
    
    // Weather Repository (singleton)
    private val weatherRepository: WeatherRepository by lazy {
        logger.info("Initializing weather repository")
        WeatherRepositoryImpl(
            weatherProvider = weatherProvider,
            weatherCache = weatherCache,
            forecastCache = forecastCache,
            locationCache = locationCache
        )
    }
    
    // Weather Service (singleton) - DDD approach
    private val weatherService: WeatherService by lazy {
        logger.info("Initializing weather application service")
        WeatherService(
            weatherRepository = weatherRepository,
            rateLimitProvider = rateLimitProvider
        )
    }

    // Public getters for dependency injection
    fun weatherService(): WeatherService = weatherService
    fun weatherRepository(): WeatherRepository = weatherRepository
    fun weatherProvider(): WeatherProvider = weatherProvider
    fun rateLimitProvider(): RateLimitProvider = rateLimitProvider
    fun weatherCache(): CacheProvider<String, WeatherData> = weatherCache
    fun forecastCache(): CacheProvider<String, WeatherForecast> = forecastCache
    fun locationCache(): CacheProvider<String, Location> = locationCache
    

    fun cleanup() {
        logger.info("Cleaning up dependencies")
        try {
            httpClient.close()
            logger.info("HTTP client closed successfully")
        } catch (e: Exception) {
            logger.error("Error closing HTTP client", e)
        }
    }
}
