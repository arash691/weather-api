package com.shape.games.com.shape.games.weather.infrastructure.config

import com.shape.games.weather.domain.exceptions.NotFoundException
import com.shape.games.weather.domain.exceptions.RateLimitExceededException
import com.shape.games.weather.domain.exceptions.ServiceUnavailableException
import com.shape.games.weather.presentation.dto.ErrorResponse
import com.shape.games.weather.presentation.dto.ErrorDetails
import com.shape.games.weather.presentation.dto.ResponseMetadata
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import kotlinx.serialization.json.Json
import org.slf4j.event.Level
import org.slf4j.LoggerFactory
import java.time.Instant

/**
 * Configure HTTP features like CORS and default headers
 */
fun Application.configureHTTP() {
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Accept)
        anyHost() // For development - restrict in production
    }
    
    install(DefaultHeaders) {
        header("X-Engine", "Ktor") // will send this header with each response
    }
}

/**
 * Configure JSON serialization
 */
fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
}

/**
 * Configure monitoring and logging
 */
fun Application.configureMonitoring() {
    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
        format { call ->
            val status = call.response.status()
            val httpMethod = call.request.httpMethod.value
            val userAgent = call.request.headers["User-Agent"]
            val requestUri = call.request.uri
            "Status: $status, HTTP method: $httpMethod, URI: $requestUri, User agent: $userAgent"
        }
    }
}

/**
 * Configure global exception handling
 */
fun Application.configureStatusPages() {
    val logger = LoggerFactory.getLogger("GlobalExceptionHandler")
    
    install(StatusPages) {
        // Handle specific exception types
        exception<IllegalArgumentException> { call, cause ->
            logger.warn("Validation error in ${call.request.uri}: ${cause.message}")
            call.respondError(
                HttpStatusCode.BadRequest,
                "VALIDATION_ERROR",
                cause.message ?: "Invalid request parameter"
            )
        }
        
        exception<NotFoundException> { call, cause ->
            logger.warn("Resource not found in ${call.request.uri}: ${cause.message}")
            call.respondError(
                HttpStatusCode.NotFound,
                "NOT_FOUND",
                cause.message ?: "Resource not found"
            )
        }
        
        exception<RateLimitExceededException> { call, cause ->
            logger.warn("Rate limit exceeded in ${call.request.uri}: ${cause.message}")
            call.respondError(
                HttpStatusCode.TooManyRequests,
                "RATE_LIMIT_EXCEEDED",
                cause.message ?: "Rate limit exceeded"
            )
        }
        
        exception<ServiceUnavailableException> { call, cause ->
            logger.error("Service unavailable in ${call.request.uri}: ${cause.message}")
            call.respondError(
                HttpStatusCode.ServiceUnavailable,
                "SERVICE_UNAVAILABLE",
                cause.message ?: "Service temporarily unavailable"
            )
        }
        
        // Handle all other exceptions
        exception<Throwable> { call, cause ->
            logger.error("Unhandled exception in ${call.request.uri}", cause)
            call.respondError(
                HttpStatusCode.InternalServerError,
                "INTERNAL_ERROR",
                "An unexpected error occurred"
            )
        }
        
        // Handle specific HTTP status codes
        status(HttpStatusCode.NotFound) { call, _ ->
            call.respondError(
                HttpStatusCode.NotFound,
                "NOT_FOUND",
                "The requested endpoint was not found"
            )
        }
        
        status(HttpStatusCode.MethodNotAllowed) { call, _ ->
            call.respondError(
                HttpStatusCode.MethodNotAllowed,
                "METHOD_NOT_ALLOWED",
                "HTTP method not allowed for this endpoint"
            )
        }
        
        status(HttpStatusCode.UnsupportedMediaType) { call, _ ->
            call.respondError(
                HttpStatusCode.UnsupportedMediaType,
                "UNSUPPORTED_MEDIA_TYPE",
                "Content type not supported"
            )
        }
    }
}

/**
 * Extension function to create consistent error responses
 */
suspend fun ApplicationCall.respondError(
    status: HttpStatusCode,
    errorCode: String,
    message: String,
    details: String? = null
) {
    val errorResponse = ErrorResponse(
        error = ErrorDetails(
            code = errorCode,
            message = message,
            details = details
        ),
        metadata = ResponseMetadata(
            timestamp = Instant.now().toString(),
            source = "weather-integration-api"
        )
    )
    respond(status, errorResponse)
}

