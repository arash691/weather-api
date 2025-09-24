package com.shape.games.weather.domain.exceptions

import org.junit.jupiter.api.Test
import kotlin.test.*

class ValidationExceptionTest {

    @Test
    fun `should create validation exception with message key`() {
        val exception = ValidationException(
            messageKey = "validation.error.required",
            message = "Field is required"
        )
        
        assertEquals("validation.error.required", exception.messageKey)
        assertEquals("Field is required", exception.message)
        assertTrue(exception.parameters.isEmpty())
        assertNull(exception.cause)
        assertTrue(exception is DomainException)
        assertTrue(exception is RuntimeException)
    }

    @Test
    fun `should create validation exception with parameters`() {
        val parameters = arrayOf<Any>("field1", 5, "field2")
        val exception = ValidationException(
            messageKey = "validation.error.range",
            parameters = parameters,
            message = "Value must be between {0} and {1}"
        )
        
        assertEquals("validation.error.range", exception.messageKey)
        assertEquals("Value must be between {0} and {1}", exception.message)
        assertEquals(3, exception.parameters.size)
        assertEquals("field1", exception.parameters[0])
        assertEquals(5, exception.parameters[1])
        assertEquals("field2", exception.parameters[2])
    }

    @Test
    fun `should create validation exception with cause`() {
        val cause = NumberFormatException("Invalid number format")
        val exception = ValidationException(
            messageKey = "validation.error.format",
            message = "Invalid format",
            cause = cause
        )
        
        assertEquals("validation.error.format", exception.messageKey)
        assertEquals("Invalid format", exception.message)
        assertEquals(cause, exception.cause)
    }
}

class NotFoundExceptionTest {

    @Test
    fun `should create not found exception`() {
        val exception = NotFoundException("Resource not found")
        
        assertEquals("Resource not found", exception.message)
        assertNull(exception.cause)
        assertTrue(exception is DomainException)
    }

    @Test
    fun `should create not found exception with cause`() {
        val cause = IllegalStateException("Database error")
        val exception = NotFoundException("Resource not found", cause)
        
        assertEquals("Resource not found", exception.message)
        assertEquals(cause, exception.cause)
    }
}

class RateLimitExceededExceptionTest {

    @Test
    fun `should create rate limit exception`() {
        val exception = RateLimitExceededException("Rate limit exceeded")
        
        assertEquals("Rate limit exceeded", exception.message)
        assertNull(exception.cause)
        assertTrue(exception is DomainException)
    }
}

class ServiceUnavailableExceptionTest {

    @Test
    fun `should create service unavailable exception`() {
        val exception = ServiceUnavailableException("Service unavailable")
        
        assertEquals("Service unavailable", exception.message)
        assertNull(exception.cause)
        assertTrue(exception is DomainException)
    }
}
