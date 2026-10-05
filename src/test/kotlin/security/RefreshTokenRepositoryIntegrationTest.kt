package com.blackneko.integration

import com.blackneko.domain.auth.RefreshToken
import com.blackneko.domain.user.Role
import com.blackneko.domain.user.User
import com.blackneko.infrastructure.database.repository.RefreshTokenRepositoryImpl
import com.blackneko.infrastructure.database.repository.UserRepositoryImpl
import org.junit.jupiter.api.Test
import kotlinx.coroutines.runBlocking
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RefreshTokenRepositoryIntegrationTest :
    DatabaseIntegrationTest() {

    private val userRepository by lazy {
        UserRepositoryImpl(
            transactionRunner
        )
    }

    private val refreshTokenRepository by lazy {
        RefreshTokenRepositoryImpl(
            transactionRunner
        )
    }

    @Test
    fun `should save and find refresh token by hash`(): Unit = runBlocking {

        val user =
            createUser()

        userRepository.save(
            user
        )

        val token =
            createToken(
                user.id
            )

        refreshTokenRepository.save(
            token
        )

        val result =
            refreshTokenRepository
                .findByHash(
                    token.tokenHash
                )

        assertNotNull(
            result
        )

        assertEquals(result.id, token.id)

        assertNull(
            result.revokedAt
        )
    }

    @Test
    fun `should revoke active token`(): Unit = runBlocking {

        val user =
            createUser()

        userRepository.save(
            user
        )

        val token =
            createToken(
                user.id
            )

        refreshTokenRepository.save(
            token
        )

        val revoked =
            refreshTokenRepository
                .revokeIfActive(
                    id =
                        token.id,

                    revokedAt =
                        Instant.now()
                )

        assertTrue(
            revoked
        )

        val result =
            refreshTokenRepository
                .findByHash(
                    token.tokenHash
                )

        assertNotNull(
            result
        )

        assertNotNull(
            result.revokedAt
        )
    }

    private fun createUser(): User {

        val now =
            Instant.now()

        return User(
            id =
                UUID.randomUUID(),

            email =
                "refresh@test.com",

            phone =
                "+5355556000",

            passwordHash =
                "hash",

            firstName =
                "Refresh",

            lastName =
                "Test",

            roles =
                setOf(
                    Role.CUSTOMER
                ),

            isActive =
                true,

            createdAt =
                now,

            updatedAt =
                now
        )
    }

    private fun createToken(
        userId: UUID
    ): RefreshToken {

        val now =
            Instant.now()

        return RefreshToken(
            id =
                UUID.randomUUID(),

            userId =
                userId,

            tokenHash =
                UUID.randomUUID()
                    .toString()
                    .replace(
                        "-",
                        ""
                    )
                    .padEnd(
                        64,
                        '0'
                    )
                    .take(
                        64
                    ),

            expiresAt =
                now.plus(
                    30,
                    ChronoUnit.DAYS
                ),

            revokedAt =
                null,

            createdAt =
                now
        )
    }
}
