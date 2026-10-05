package com.blackneko.presentation.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.blackneko.infrastructure.security.JwtConfig
import com.blackneko.domain.user.Role
import com.blackneko.presentation.common.ErrorResponse
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.respond
import org.koin.ktor.ext.inject
import java.util.UUID

const val JWT_AUTH = "auth-jwt"

fun Application.configureAuthentication(config: JwtConfig? = null) {

    val jwtConfig = config ?: inject<JwtConfig>().value

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

                    if (credential.payload.expiresAt == null) return@validate null
                    val roles = credential.payload.getClaim("roles").asList(String::class.java)
                    if (roles.isNullOrEmpty() || roles.any { role -> Role.entries.none { it.name == role } }) return@validate null

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
                    ErrorResponse("UNAUTHORIZED", "Authentication required")
                )
            }
        }
    }
}
