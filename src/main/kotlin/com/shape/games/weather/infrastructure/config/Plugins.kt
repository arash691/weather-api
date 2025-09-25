package com.shape.games.weather.infrastructure.config

import com.shape.games.weather.domain.exceptions.NotFoundException
import com.shape.games.weather.domain.exceptions.RateLimitExceededException
import com.shape.games.weather.domain.exceptions.ServiceUnavailableException
import com.shape.games.weather.domain.exceptions.ValidationException
import com.shape.games.weather.presentation.dto.ErrorDetails
import com.shape.games.weather.presentation.dto.ErrorResponse
import com.shape.games.weather.presentation.dto.ResponseMetadata
import io.ktor.http.*
import io.ktor.i18n.*
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
import org.slf4j.LoggerFactory
import org.slf4j.event.Level
import java.text.MessageFormat
import java.time.Instant

fun Application.configureHTTP() {
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Accept)
        anyHost()
    }

    install(DefaultHeaders) {
        header("X-Engine", "Ktor")
    }
}


fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
}


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

fun Application.configureI18n() {
    install(I18n) {}
}


fun Application.configureStatusPages() {
    val logger = LoggerFactory.getLogger("GlobalExceptionHandler")

    install(StatusPages) {
        exception<ValidationException> { call, cause ->
            logger.warn("Validation error in ${call.request.uri}: ${cause.messageKey}")
            call.respondError(
                HttpStatusCode.BadRequest,
                "VALIDATION_ERROR",
                call.getMessage(cause.messageKey, *cause.parameters),
                details = call.getMessage("validation.error")
            )
        }

        exception<IllegalArgumentException> { call, cause ->
            logger.warn("Generic validation error in ${call.request.uri}: ${cause.message}")
            call.respondError(
                HttpStatusCode.BadRequest,
                "VALIDATION_ERROR",
                cause.message ?: call.getMessage("validation.error"),
                details = call.getMessage("validation.error")
            )
        }

        exception<NotFoundException> { call, cause ->
            logger.warn("Resource not found in ${call.request.uri}: ${cause.message}")
            call.respondError(
                HttpStatusCode.NotFound,
                "NOT_FOUND",
                cause.message ?: call.getMessage("error.not_found")
            )
        }

        exception<RateLimitExceededException> { call, cause ->
            logger.warn("Rate limit exceeded in ${call.request.uri}: ${cause.message}")
            call.respondError(
                HttpStatusCode.TooManyRequests,
                "RATE_LIMIT_EXCEEDED",
                cause.message ?: call.getMessage("ratelimit.exceeded")
            )
        }


        status(HttpStatusCode.TooManyRequests) { call, status ->
            val retryAfter = call.response.headers["Retry-After"] ?: "3600"
            val rateLimitType = when {
                call.request.uri.contains("/weather") -> "weather API"
                else -> "API"
            }

            call.respondError(
                HttpStatusCode.TooManyRequests,
                "RATE_LIMIT_EXCEEDED",
                call.getMessage("ratelimit.exceeded.detailed", rateLimitType, retryAfter),
                details = call.getMessage("ratelimit.exceeded")
            )
        }

        exception<ServiceUnavailableException> { call, cause ->
            logger.error("Service unavailable in ${call.request.uri}: ${cause.message}")
            call.respondError(
                HttpStatusCode.ServiceUnavailable,
                "SERVICE_UNAVAILABLE",
                cause.message ?: call.getMessage("error.service_unavailable")
            )
        }

        exception<Throwable> { call, cause ->
            logger.error("Unhandled exception in ${call.request.uri}", cause)
            call.respondError(
                HttpStatusCode.InternalServerError,
                "INTERNAL_ERROR",
                call.getMessage("error.internal_error")
            )
        }

        status(HttpStatusCode.NotFound) { call, _ ->
            call.respondError(
                HttpStatusCode.NotFound,
                "NOT_FOUND",
                call.getMessage("error.not_found")
            )
        }

        status(HttpStatusCode.MethodNotAllowed) { call, _ ->
            call.respondError(
                HttpStatusCode.MethodNotAllowed,
                "METHOD_NOT_ALLOWED",
                call.getMessage("error.method_not_allowed")
            )
        }

        status(HttpStatusCode.UnsupportedMediaType) { call, _ ->
            call.respondError(
                HttpStatusCode.UnsupportedMediaType,
                "UNSUPPORTED_MEDIA_TYPE",
                call.getMessage("error.unsupported_media_type")
            )
        }
    }
}

suspend fun ApplicationCall.respondError(
    status: HttpStatusCode,
    errorCode: String,
    message: String,
    details: String? = null
) {

    val requestId = "err_${java.util.UUID.randomUUID().toString().take(8)}"
    response.headers.append("X-Request-ID", requestId)
    response.headers.append("X-Error-Code", errorCode)

    response.headers.append("Access-Control-Allow-Origin", "*")
    response.headers.append("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
    response.headers.append("Access-Control-Allow-Headers", "Content-Type, Authorization")

    val errorResponse = ErrorResponse(
        error = ErrorDetails(
            code = errorCode,
            message = message,
            details = details
        ),
        metadata = ResponseMetadata(
            timestamp = Instant.now().atOffset(java.time.ZoneOffset.UTC)
                .format(java.time.format.DateTimeFormatter.ISO_INSTANT),
            requestId = requestId
        )
    )
    respond(status, errorResponse)
}

fun ApplicationCall.getMessage(key: String, vararg args: Any): String {
    return try {
        val message = this.i18n(key)
        if (args.isNotEmpty()) {
            MessageFormat.format(message, *args)
        } else {
            message
        }
    } catch (e: Exception) {
        "!$key!"
    }
}


