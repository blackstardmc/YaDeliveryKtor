package com.blackneko.presentation.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.blackneko.infrastructure.security.JwtConfig
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.respond
import org.koin.ktor.ext.inject
import java.util.UUID

const val JWT_AUTH = "auth-jwt"

fun Application.configureAuthentication() {

    val jwtConfig by inject<JwtConfig>()

    val algorithm =
        Algorithm.HMAC256(
            jwtConfig.secret
        )

    install(Authentication) {

        jwt(JWT_AUTH) {

            realm =
                jwtConfig.realm

            verifier(
                JWT
                    .require(algorithm)
                    .withIssuer(
                        jwtConfig.issuer
                    )
                    .withAudience(
                        jwtConfig.audience
                    )
                    .build()
            )

            validate { credential ->

                val subject =
                    credential.payload.subject
                        ?: return@validate null

                try {

                    UUID.fromString(
                        subject
                    )

                    JWTPrincipal(
                        credential.payload
                    )

                } catch (_: IllegalArgumentException) {

                    null
                }
            }

            challenge { _, _ ->

                call.respond(
                    io.ktor.http.HttpStatusCode.Unauthorized,
                    mapOf(
                        "error" to "unauthorized",
                        "message" to
                                "Authentication required"
                    )
                )
            }
        }
    }
}