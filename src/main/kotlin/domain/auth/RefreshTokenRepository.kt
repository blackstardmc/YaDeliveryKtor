package com.blackneko.domain.auth

import java.time.Instant
import java.util.UUID

interface RefreshTokenRepository {

    suspend fun save(
        token: RefreshToken
    )

    suspend fun findByHash(
        tokenHash: String
    ): RefreshToken?

    suspend fun revokeIfActive(
        id: UUID,
        revokedAt: Instant
    ): Boolean

    suspend fun revokeAllByUser(
        userId: UUID,
        revokedAt: Instant
    )

    suspend fun deleteExpired(
        now: Instant
    ): Int
}