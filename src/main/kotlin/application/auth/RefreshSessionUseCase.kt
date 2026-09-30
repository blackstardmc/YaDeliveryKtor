package com.blackneko.application.auth

import com.blackneko.application.TransactionRunner
import com.blackneko.application.exception.AuthenticationException
import com.blackneko.application.security.RefreshTokenGenerator
import com.blackneko.application.security.TokenService
import com.blackneko.application.security.TokenSettings
import com.blackneko.domain.auth.RefreshToken
import com.blackneko.domain.auth.RefreshTokenRepository
import com.blackneko.domain.user.UserRepository
import java.time.Clock
import java.time.temporal.ChronoUnit
import java.util.UUID

class RefreshSessionUseCase(
    private val refreshTokenRepository:
    RefreshTokenRepository,

    private val userRepository:
    UserRepository,

    private val refreshTokenGenerator:
    RefreshTokenGenerator,

    private val tokenService:
    TokenService,

    private val transactionRunner:
    TransactionRunner,

    private val clock:
    Clock,

    private val settings:
    TokenSettings
) {

    suspend operator fun invoke(
        rawRefreshToken: String
    ): AuthSession {

        if (rawRefreshToken.isBlank()) {
            throw AuthenticationException()
        }

        val now =
            clock.instant()

        val hash =
            refreshTokenGenerator.hash(
                rawRefreshToken
            )

        return transactionRunner.transaction {

            val current =
                refreshTokenRepository
                    .findByHash(hash)
                    ?: throw AuthenticationException()

            if (!current.isValid(now)) {
                throw AuthenticationException()
            }

            val user =
                userRepository
                    .findById(
                        current.userId
                    )
                    ?: throw AuthenticationException()

            if (!user.isActive) {
                throw AuthenticationException()
            }

            /*
             * Rotación:
             * el token utilizado deja de ser válido.
             */
            val revoked =
                refreshTokenRepository
                    .revokeIfActive(
                        id =
                            current.id,

                        revokedAt =
                            now
                    )

            if (!revoked) {
                throw AuthenticationException()
            }

            val rawNewToken =
                refreshTokenGenerator.generate()

            val newToken =
                RefreshToken(
                    id =
                        UUID.randomUUID(),

                    userId =
                        user.id,

                    tokenHash =
                        refreshTokenGenerator.hash(
                            rawNewToken
                        ),

                    expiresAt =
                        now.plus(
                            settings
                                .refreshTokenExpirationDays,
                            ChronoUnit.DAYS
                        ),

                    revokedAt =
                        null,

                    createdAt =
                        now
                )

            refreshTokenRepository.save(
                newToken
            )

            AuthSession(
                userId =
                    user.id,

                roles =
                    user.roles,

                accessToken =
                    tokenService
                        .generateAccessToken(
                            user
                        ),

                refreshToken =
                    rawNewToken
            )
        }
    }
}