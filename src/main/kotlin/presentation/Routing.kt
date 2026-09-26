package com.blackneko.presentation


import com.blackneko.presentation.auth.authRoutes
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.swagger.*

fun Application.configureRouting() {

    routing {

        get("/") {

            call.respond(
                HttpStatusCode.OK,
                mapOf(
                    "name" to "Delivery API",
                    "version" to "0.1.0",
                    "status" to "running"
                )
            )
        }

        get("/health") {

            call.respond(
                HttpStatusCode.OK,
                mapOf(
                    "status" to "UP"
                )
            )
        }
        authRoutes()
        swaggerUI(
            path = "swagger",
            swaggerFile = "openapi/documentation.yaml"
        )
    }
}