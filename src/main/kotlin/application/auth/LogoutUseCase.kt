package com.blackneko.application.auth

import com.blackneko.application.security.RefreshTokenGenerator
import com.blackneko.domain.auth.RefreshTokenRepository
import java.time.Clock

class LogoutUseCase(
    private val refreshTokenRepository:
    RefreshTokenRepository,

    private val refreshTokenGenerator:
    RefreshTokenGenerator,

    private val clock:
    Clock
) {

    suspend operator fun invoke(
        rawRefreshToken: String
    ) {

        if (rawRefreshToken.isBlank()) {
            return
        }

        val hash =
            refreshTokenGenerator.hash(
                rawRefreshToken
            )

        val token =
            refreshTokenRepository
                .findByHash(hash)
                ?: return

        if (token.revokedAt != null) {
            return
        }

        refreshTokenRepository.revoke(
            id =
                token.id,

            revokedAt =
                clock.instant()
        )
    }
}