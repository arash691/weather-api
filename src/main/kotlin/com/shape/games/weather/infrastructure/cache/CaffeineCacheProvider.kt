package com.shape.games.weather.infrastructure.cache

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import com.shape.games.weather.domain.cache.CacheProvider
import com.shape.games.weather.domain.cache.CacheStats
import org.slf4j.LoggerFactory
import kotlin.time.toJavaDuration

/**
 * Caffeine-based cache provider implementation
 * High-performance in-memory cache with TTL and size limits
 */
class CaffeineCacheProvider<K : Any, V : Any>(
    private val config: com.shape.games.weather.domain.cache.CacheConfig
) : CacheProvider<K, V> {

    private val logger = LoggerFactory.getLogger(CaffeineCacheProvider::class.java)

    private val cache: Cache<K, V> = Caffeine.newBuilder()
        .maximumSize(config.maxSize)
        .expireAfterWrite(config.expireAfterWrite.toJavaDuration())
        .apply {
            config.expireAfterAccess?.let {
                expireAfterAccess(it.toJavaDuration())
            }
        }
        .apply {
            if (config.enableStats) recordStats()
        }
        .build()

    override suspend fun get(key: K): V? {
        val value = cache.getIfPresent(key)
        if (value != null) {
            logger.debug("Cache hit for key: {} in namespace: {}", key, config.namespace)
        } else {
            logger.debug("Cache miss for key: {} in namespace: {}", key, config.namespace)
        }
        return value
    }

    override suspend fun put(key: K, value: V) {
        cache.put(key, value)
        logger.debug("Cached value for key: {} in namespace: {}", key, config.namespace)
    }

    override suspend fun getOrLoad(key: K, loader: suspend (K) -> V?): V? {
        return get(key) ?: run {
            val computed = loader(key)
            computed?.let { put(key, it) }
            computed
        }
    }

    override suspend fun invalidate(key: K) {
        cache.invalidate(key)
        logger.debug("Invalidated cache for key: {} in namespace: {}", key, config.namespace)
    }

    override suspend fun invalidateAll() {
        cache.invalidateAll()
        logger.debug("Cleared all cache entries in namespace: {}", config.namespace)
    }

    override suspend fun getStats(): CacheStats {
        val stats = cache.stats()
        return CacheStats(
            hitCount = stats.hitCount(),
            missCount = stats.missCount(),
            hitRate = stats.hitRate(),
            size = cache.estimatedSize(),
            maxSize = config.maxSize,
            evictionCount = stats.evictionCount()
        )
    }

    override suspend fun size(): Long = cache.estimatedSize()

    override suspend fun isHealthy(): Boolean = true

    /**
     * Get cache configuration
     */
    fun getConfig(): com.shape.games.weather.domain.cache.CacheConfig = config

    /**
     * Get cache namespace
     */
    fun getNamespace(): String = config.namespace
}
