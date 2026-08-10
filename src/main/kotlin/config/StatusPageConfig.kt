package com.blackneko.config

import io.ktor.http.*
import io.ktor.server.application.*
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
                mapOf(
                    "error" to "INTERNAL_SERVER_ERROR",
                    "message" to "An unexpected error occurred"
                )
            )
        }
    }
}