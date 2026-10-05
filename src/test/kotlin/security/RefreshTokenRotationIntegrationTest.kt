package com.blackneko.integration

import com.blackneko.domain.auth.RefreshToken
import com.blackneko.domain.user.Role
import com.blackneko.domain.user.User
import com.blackneko.infrastructure.database.repository.RefreshTokenRepositoryImpl
import com.blackneko.infrastructure.database.repository.UserRepositoryImpl
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID
import kotlin.test.assertEquals

class RefreshTokenRotationIntegrationTest :
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
    fun `only one concurrent refresh should revoke token`() =
        runBlocking {

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

            val now =
                Instant.now()

            val results =
                coroutineScope {

                    listOf(
                        async {

                            refreshTokenRepository
                                .revokeIfActive(
                                    token.id,
                                    now
                                )
                        },

                        async {

                            refreshTokenRepository
                                .revokeIfActive(
                                    token.id,
                                    now
                                )
                        }
                    )
                        .awaitAll()
                }

            assertEquals(
                1,
                results.count {
                    it
                }
            )

            assertEquals(
                1,
                results.count {
                    !it
                }
            )
        }

    private fun createUser(): User {

        val now =
            Instant.now()

        return User(
            id =
                UUID.randomUUID(),

            email =
                "concurrent@test.com",

            phone =
                "+5355557000",

            passwordHash =
                "hash",

            firstName =
                "Concurrent",

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
                        'a'
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