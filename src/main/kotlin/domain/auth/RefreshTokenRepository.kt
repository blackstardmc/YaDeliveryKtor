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

    suspend fun revoke(
        id: UUID,
        revokedAt: Instant
    )

    suspend fun revokeAllByUser(
        userId: UUID,
        revokedAt: Instant
    )

    suspend fun deleteExpired(
        now: Instant
    ): Int
}