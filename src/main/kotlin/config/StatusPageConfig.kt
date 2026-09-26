package com.blackneko.config

import com.blackneko.application.exception.AuthenticationException
import com.blackneko.application.exception.AuthorizationException
import com.blackneko.application.exception.ConflictException
import com.blackneko.presentation.common.ErrorResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

fun Application.configureStatusPages() {

    install(StatusPages) {

        exception<Throwable> { call, cause ->

            this@configureStatusPages.log.error(
                "Unhandled exception",
                cause
            )

            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse(
                    error = "INTERNAL_SERVER_ERROR",
                    message = "An unexpected error occurred"
                )
            )
        }

        exception<AuthenticationException> { call,
                                             cause ->

            call.respond(
                HttpStatusCode.Unauthorized, ErrorResponse(
                    error = "unauthorized",
                    message = cause.message ?: "unauthorized"
                )

            )
        }

        exception<AuthorizationException> { call,
                                            cause ->

            call.respond(
                HttpStatusCode.Forbidden,
                ErrorResponse(
                    error = "forbidden",
                    message = cause.message
                        ?: "Access denied"
                )
            )
        }

        exception<NotFoundException> { call,
                                       cause ->

            call.respond(
                HttpStatusCode.NotFound,
                ErrorResponse(
                    error = "not_found",
                    message = cause.message
                        ?: "Not founded"
                )
            )
        }

        exception<ConflictException> { call,
                                       cause ->

            call.respond(
                HttpStatusCode.Conflict,
                ErrorResponse(
                    error = "Conflict",
                    message = cause.message
                        ?: "Conflict"
                )
            )
        }
    }
}