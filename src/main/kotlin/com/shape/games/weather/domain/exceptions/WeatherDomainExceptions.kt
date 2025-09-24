package com.shape.games.weather.domain.exceptions

/**
 * Domain exceptions for weather-related business rules
 * These represent business rule violations and domain-specific errors
 */

/**
 * Exception thrown when a requested resource is not found
 * This is a domain concept - the business rule that a resource must exist
 */
class NotFoundException(message: String, cause: Throwable? = null) : DomainException(message, cause)

/**
 * Exception thrown when request validation fails with specific validation rules
 * Contains the i18n message key for proper localization
 */
class ValidationException(
    val messageKey: String,
    val parameters: Array<Any> = emptyArray(),
    message: String,
    cause: Throwable? = null
) : DomainException(message, cause)

/**
 * Exception thrown when rate limit is exceeded
 * This represents a business rule about API usage limits
 */
class RateLimitExceededException(message: String, cause: Throwable? = null) : DomainException(message, cause)

/**
 * Exception thrown when an external service is unavailable
 * This represents a business rule about service availability requirements
 */
class ServiceUnavailableException(message: String, cause: Throwable? = null) : DomainException(message, cause)


/**
 * Base class for all domain exceptions
 * Represents violations of business rules and domain invariants
 */
abstract class DomainException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
