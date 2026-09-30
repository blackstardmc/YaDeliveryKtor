package com.blackneko.application.auth

import com.blackneko.domain.auth.RefreshTokenRepository
import java.time.Clock
import java.util.UUID

class LogoutAllUseCase(
    private val refreshTokenRepository: RefreshTokenRepository,
    private val clock: Clock
) {

    suspend operator fun invoke(
        userId: UUID
    ) {

        refreshTokenRepository
            .revokeAllByUser(
                userId =
                    userId,

                revokedAt =
                    clock.instant()
            )
    }
}