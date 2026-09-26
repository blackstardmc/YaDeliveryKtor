package com.blackneko.application.auth

import com.blackneko.application.TransactionRunner
import com.blackneko.application.security.RefreshTokenGenerator
import com.blackneko.application.security.TokenService
import com.blackneko.application.security.TokenSettings
import com.blackneko.domain.auth.RefreshToken
import com.blackneko.domain.auth.RefreshTokenRepository
import com.blackneko.domain.user.User
import java.time.Clock
import java.time.temporal.ChronoUnit
import java.util.UUID

class CreateAuthSessionUseCase(
    private val tokenService: TokenService,
    private val refreshTokenGenerator:
    RefreshTokenGenerator,
    private val refreshTokenRepository:
    RefreshTokenRepository,
    private val transactionRunner:
    TransactionRunner,
    private val clock: Clock,
    private val tokenSettings: TokenSettings
) {

    suspend operator fun invoke(
        user: User
    ): AuthSession {

        val now =
            clock.instant()

        val rawRefreshToken =
            refreshTokenGenerator.generate()

        val refreshToken =
            RefreshToken(
                id =
                    UUID.randomUUID(),

                userId =
                    user.id,

                tokenHash =
                    refreshTokenGenerator.hash(
                        rawRefreshToken
                    ),

                expiresAt =
                    now.plus(
                        tokenSettings
                            .refreshTokenExpirationDays,
                        ChronoUnit.DAYS
                    ),

                revokedAt =
                    null,

                createdAt =
                    now
            )

        transactionRunner.transaction {

            refreshTokenRepository.save(
                refreshToken
            )
        }

        return AuthSession(
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
                rawRefreshToken
        )
    }
}