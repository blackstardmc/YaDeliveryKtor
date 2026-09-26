package com.blackneko.presentation.security

import com.blackneko.application.security.AuthenticatedUser
import com.blackneko.domain.user.Role
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import java.util.UUID

fun ApplicationCall.authenticatedUser():
        AuthenticatedUser {

    val principal =
        principal<JWTPrincipal>()
            ?: throw IllegalStateException(
                "Authenticated principal not found"
            )

    val userId =
        UUID.fromString(
            principal.payload.subject
        )

    val roles =
        principal.payload
            .getClaim("roles")
            .asList(String::class.java)
            .orEmpty()
            .mapNotNull { role ->

                runCatching {
                    Role.valueOf(role)
                }.getOrNull()
            }
            .toSet()

    return AuthenticatedUser(
        userId =
            userId,

        roles =
            roles
    )
}