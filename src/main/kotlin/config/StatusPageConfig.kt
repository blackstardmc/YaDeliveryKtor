package com.blackneko.config

import com.blackneko.application.exception.*
import com.blackneko.presentation.common.ErrorResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<ValidationException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("VALIDATION_ERROR", cause.message ?: "Invalid request", cause.field))
        }
        exception<BadRequestException> { call, _ ->
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("VALIDATION_ERROR", "Invalid request body"))
        }
        exception<AuthenticationException> { call, _ ->
            call.respond(HttpStatusCode.Unauthorized, ErrorResponse("UNAUTHORIZED", "Invalid credentials"))
        }
        exception<AuthorizationException> { call, _ ->
            call.respond(HttpStatusCode.Forbidden, ErrorResponse("FORBIDDEN", "Access denied"))
        }
        exception<NotFoundException> { call, cause ->
            call.respond(HttpStatusCode.NotFound, ErrorResponse("NOT_FOUND", cause.message ?: "Resource not found"))
        }
        exception<ConflictException> { call, cause ->
            call.respond(HttpStatusCode.Conflict, ErrorResponse("CONFLICT", cause.message ?: "Conflict"))
        }
        status(HttpStatusCode.TooManyRequests) { call, status ->
            call.respond(status, ErrorResponse("RATE_LIMITED", "Too many requests"))
        }
        exception<Throwable> { call, cause ->
            // Messages may contain SQL parameters, credentials or request bodies.
            this@configureStatusPages.log.error("Unhandled exception of type {}", cause.javaClass.name)
            call.respond(HttpStatusCode.InternalServerError, ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred"))
        }
    }
}
