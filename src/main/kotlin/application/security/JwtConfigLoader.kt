package com.blackneko.application.security

import com.blackneko.infrastructure.security.JwtConfig
import io.ktor.server.config.*

fun ApplicationConfig.jwtConfig(): JwtConfig =
    JwtConfig(
        issuer =
            property("jwt.issuer")
                .getString(),

        audience =
            property("jwt.audience")
                .getString(),

        realm =
            property("jwt.realm")
                .getString(),

        secret =
            property("jwt.secret")
                .getString(),

        accessTokenExpirationMinutes =
            property(
                "jwt.accessTokenExpirationMinutes"
            )
                .getString()
                .toLong(),
        refreshTokenExpirationDays =
            property(
                "jwt.refreshTokenExpirationDays"
            )
                .getString()
                .toLong()
    )