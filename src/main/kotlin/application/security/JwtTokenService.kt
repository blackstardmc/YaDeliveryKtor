package com.blackneko.application.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.blackneko.application.security.JwtConfig
import com.blackneko.application.security.TokenService
import com.blackneko.domain.user.User
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date

class JwtTokenService(
    private val config: JwtConfig
) : TokenService {

    private val algorithm =
        Algorithm.HMAC256(
            config.secret
        )

    override fun generateAccessToken(
        user: User
    ): String {

        val now =
            Instant.now()

        val expiresAt =
            now.plus(
                config.accessTokenExpirationMinutes,
                ChronoUnit.MINUTES
            )

        return JWT.create()
            .withIssuer(
                config.issuer
            )
            .withAudience(
                config.audience
            )
            .withSubject(
                user.id.toString()
            )
            .withClaim(
                "roles",
                user.roles.map {
                    it.name
                }
            )
            .withIssuedAt(
                Date.from(now)
            )
            .withExpiresAt(
                Date.from(expiresAt)
            )
            .sign(
                algorithm
            )
    }
}