package com.shape.games.weather.domain.cache

import kotlin.time.Duration

/**
 * Abstract interface for cache implementations
 * Allows switching between different cache providers (Caffeine, Redis, Hazelcast, etc.)
 */
interface CacheProvider<K : Any, V : Any> {
    
    /**
     * Get value from cache
     * @param key The cache key
     * @return Cached value or null if not present
     */
    suspend fun get(key: K): V?
    
    /**
     * Put value in cache
     * @param key The cache key
     * @param value The value to cache
     */
    suspend fun put(key: K, value: V)
    
    /**
     * Get value from cache or compute if not present
     * @param key The cache key
     * @param loader Function to compute value if not cached
     * @return The cached or computed value
     */
    suspend fun getOrLoad(key: K, loader: suspend (K) -> V?): V?
    
    /**
     * Invalidate cache entry
     * @param key The cache key to invalidate
     */
    suspend fun invalidate(key: K)
    
    /**
     * Clear all cache entries
     */
    suspend fun invalidateAll()
    
    /**
     * Get cache statistics
     */
    suspend fun getStats(): CacheStats
    
    /**
     * Get current cache size
     */
    suspend fun size(): Long
    
    /**
     * Check if the cache is healthy
     */
    suspend fun isHealthy(): Boolean
}

/**
 * Cache statistics
 */
data class CacheStats(
    val hitCount: Long,
    val missCount: Long,
    val hitRate: Double,
    val size: Long,
    val maxSize: Long,
    val evictionCount: Long = 0
)

/**
 * Cache configuration
 */
data class CacheConfig(
    val providerType: CacheProviderType,
    val maxSize: Long = 1000,
    val expireAfterWrite: Duration = Duration.parse("PT15M"),
    val expireAfterAccess: Duration? = null,
    val enableStats: Boolean = true,
    val namespace: String = "default"
)

/**
 * Supported cache provider types
 */
enum class CacheProviderType {
    CAFFEINE,
    REDIS,
    HAZELCAST,
    EHCACHE,
    MEMORY
}

/**
 * Cache serialization interface for distributed caches
 */
interface CacheSerializer<T> {
    fun serialize(value: T): String
    fun deserialize(data: String): T
}
