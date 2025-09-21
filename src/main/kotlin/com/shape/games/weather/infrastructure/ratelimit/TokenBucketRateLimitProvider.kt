package com.shape.games.weather.infrastructure.ratelimit

import com.shape.games.weather.domain.ratelimit.RateLimitProvider
import com.shape.games.weather.domain.ratelimit.RateLimitStats
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import kotlin.time.Duration

/**
 * Token Bucket implementation of rate limiting
 * More efficient than sliding window for burst handling
 */
class TokenBucketRateLimitProvider(
    private val maxRequests: Int,
    private val windowSize: Duration,
    private val burstAllowance: Double = 0.2
) : RateLimitProvider {
    
    private val logger = LoggerFactory.getLogger(TokenBucketRateLimitProvider::class.java)
    private val mutex = Mutex()
    
    // Token bucket state
    private val tokens = AtomicInteger(maxRequests)
    private val lastRefill = AtomicLong(System.currentTimeMillis())
    
    // Refill rate: requests per millisecond
    private val refillRate = maxRequests.toDouble() / windowSize.inWholeMilliseconds
    
    // Statistics
    private val consumedRequests = AtomicInteger(0)
    private val totalRequests = AtomicInteger(0)
    
    override suspend fun canMakeRequest(): Boolean = mutex.withLock {
        refillTokens()
        val available = tokens.get()
        logger.debug("Token bucket check: {} tokens available", available)
        available > 0
    }
    
    override suspend fun consumeToken(): Boolean = mutex.withLock {
        refillTokens()
        val available = tokens.get()
        
        if (available > 0) {
            tokens.decrementAndGet()
            consumedRequests.incrementAndGet()
            totalRequests.incrementAndGet()
            logger.debug("Token consumed. Remaining: {}", tokens.get())
            true
        } else {
            totalRequests.incrementAndGet()
            logger.warn("Token bucket empty. Request rejected")
            false
        }
    }
    
    override suspend fun getRemainingRequests(): Int = mutex.withLock {
        refillTokens()
        tokens.get()
    }
    
    override suspend fun getTimeUntilReset(): Duration = mutex.withLock {
        refillTokens()
        val now = System.currentTimeMillis()
        val timeSinceLastRefill = now - lastRefill.get()
        val timeUntilNextRefill = windowSize.inWholeMilliseconds - timeSinceLastRefill
        
        if (timeUntilNextRefill > 0) {
            Duration.parse("PT${timeUntilNextRefill / 1000}S")
        } else {
            Duration.ZERO
        }
    }
    
    override suspend fun getStats(): RateLimitStats = mutex.withLock {
        refillTokens()
        val resetTime = lastRefill.get() + windowSize.inWholeMilliseconds
        
        RateLimitStats(
            maxRequests = maxRequests,
            remainingRequests = tokens.get(),
            consumedRequests = consumedRequests.get(),
            resetTime = resetTime,
            algorithm = "TOKEN_BUCKET"
        )
    }
    
    override suspend fun isHealthy(): Boolean = true
    
    /**
     * Refill tokens based on time passed
     */
    private fun refillTokens() {
        val now = System.currentTimeMillis()
        val timePassed = now - lastRefill.get()
        
        if (timePassed > 0) {
            val tokensToAdd = (timePassed * refillRate).toInt()
            
            if (tokensToAdd > 0) {
                val newTokenCount = tokens.updateAndGet { current ->
                    minOf(maxRequests, current + tokensToAdd)
                }
                
                lastRefill.set(now)
                logger.debug("Refilled {} tokens. Total: {}", tokensToAdd, newTokenCount)
            }
        }
    }
}
