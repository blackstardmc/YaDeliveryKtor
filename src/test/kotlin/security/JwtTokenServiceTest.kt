package com.blackneko.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.blackneko.application.security.JwtTokenService
import com.blackneko.domain.user.Role
import com.blackneko.domain.user.User
import com.blackneko.infrastructure.security.JwtConfig
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class JwtTokenServiceTest {

    private val config =
        JwtConfig(
            issuer =
                "delivery-test",

            audience =
                "delivery-test-client",

            realm =
                "Delivery Test",

            secret =
                "test-secret-with-at-least-32-characters",

            accessTokenExpirationMinutes =
                15,

            refreshTokenExpirationDays =
                30
        )

    private val tokenService =
        JwtTokenService(
            config
        )

    @Test
    fun `should generate valid signed JWT`() {

        val user =
            createUser()

        val token =
            tokenService
                .generateAccessToken(
                    user
                )

        val verifier =
            JWT.require(
                Algorithm.HMAC256(
                    config.secret
                )
            )
                .withIssuer(
                    config.issuer
                )
                .withAudience(
                    config.audience
                )
                .build()

        val decoded =
            verifier.verify(
                token
            )

        assertEquals(
            user.id.toString(),
            decoded.subject
        )

        assertEquals(
            config.issuer,
            decoded.issuer
        )

        assertTrue(
            decoded.audience.contains(
                config.audience
            )
        )
    }

    @Test
    fun `jwt should contain user roles`() {

        val user =
            createUser(
                roles =
                    setOf(
                        Role.CUSTOMER,
                        Role.DRIVER
                    )
            )

        val token =
            tokenService
                .generateAccessToken(
                    user
                )

        val decoded =
            JWT.decode(
                token
            )

        val roles =
            decoded
                .getClaim(
                    "roles"
                )
                .asList(
                    String::class.java
                )
                .toSet()

        assertEquals(
            setOf(
                "CUSTOMER",
                "DRIVER"
            ),
            roles
        )
    }

    @Test
    fun `jwt should contain expiration`() {

        val user =
            createUser()

        val token =
            tokenService
                .generateAccessToken(
                    user
                )

        val decoded =
            JWT.decode(
                token
            )

        assertNotNull(
            decoded.expiresAt
        )

        assertTrue(
            decoded.expiresAt
                .toInstant()
                .isAfter(
                    Instant.now()
                )
        )
    }

    @Test
    fun `token should fail verification with wrong secret`() {

        val token =
            tokenService.generateAccessToken(
                createUser()
            )

        val wrongVerifier =
            JWT.require(
                Algorithm.HMAC256(
                    "completely-different-secret-123456789"
                )
            )
                .withIssuer(
                    config.issuer
                )
                .withAudience(
                    config.audience
                )
                .build()

        kotlin.test.assertFails {
            wrongVerifier.verify(
                token
            )
        }
    }

    private fun createUser(
        roles: Set<Role> =
            setOf(
                Role.CUSTOMER
            )
    ): User {

        val now =
            Instant.now()

        return User(
            id =
                UUID.randomUUID(),

            email =
                "jwt@test.com",

            phone =
                "+5355000000",

            passwordHash =
                "hash",

            firstName =
                "JWT",

            lastName =
                "Test",

            roles =
                roles,

            isActive =
                true,

            createdAt =
                now,

            updatedAt =
                now
        )
    }
}