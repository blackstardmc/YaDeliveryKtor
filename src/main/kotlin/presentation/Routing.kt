package com.blackneko.presentation


import com.blackneko.presentation.auth.authRoutes
import com.blackneko.presentation.marketplace.marketplaceRoutes
import com.blackneko.application.ReadinessProbe
import org.koin.ktor.ext.inject
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.swagger.*

fun Application.configureRouting() {

    routing {
        val readiness by inject<ReadinessProbe>()
        get("/live") { call.respond(mapOf("status" to "UP")) }
        get("/openapi.json") { call.respondResource("openapi/documentation.json") }
        get("/ready") {
            val ready = readiness.isReady()
            call.respond(if (ready) HttpStatusCode.OK else HttpStatusCode.ServiceUnavailable,
                mapOf("status" to if (ready) "UP" else "DOWN"))
        }

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
        marketplaceRoutes()
        swaggerUI(
            path = "swagger",
            swaggerFile = "openapi/documentation.json"
        )
    }
}
