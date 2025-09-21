package com.shape.games.weather.domain.ratelimit

import kotlin.time.Duration

/**
 * Abstract interface for rate limiting implementations
 * Allows switching between different rate limiting algorithms
 */
interface RateLimitProvider {

    /**
     * Check if a request can be made without exceeding the rate limit
     * @return true if request can be made, false otherwise
     */
    suspend fun canMakeRequest(): Boolean

    /**
     * Consume a token/request from the rate limiter
     * @return true if token was consumed, false if rate limit exceeded
     */
    suspend fun consumeToken(): Boolean

    /**
     * Get the remaining number of requests allowed
     */
    suspend fun getRemainingRequests(): Int

    /**
     * Get the time until the rate limit resets
     */
    suspend fun getTimeUntilReset(): Duration

    /**
     * Get current rate limit statistics
     */
    suspend fun getStats(): RateLimitStats

    /**
     * Check if the rate limiter is healthy
     */
    suspend fun isHealthy(): Boolean
}

/**
 * Rate limit statistics
 */
data class RateLimitStats(
    val maxRequests: Int,
    val remainingRequests: Int,
    val consumedRequests: Int,
    val resetTime: Long,
    val algorithm: String
)

/**
 * Rate limit configuration
 */
data class RateLimitConfig(
    val maxRequests: Int,
    val windowSize: Duration,
    val algorithm: RateLimitAlgorithm,
    val burstAllowance: Double = 0.2, // 20% burst allowance
    val refillRate: Double? = null // requests per second
)

/**
 * Supported rate limiting algorithms
 */
enum class RateLimitAlgorithm {
    TOKEN_BUCKET,
    SLIDING_WINDOW,
    FIXED_WINDOW,
    LEAKY_BUCKET
}
