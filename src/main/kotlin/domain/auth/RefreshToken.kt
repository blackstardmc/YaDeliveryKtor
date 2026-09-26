package com.blackneko.domain.auth

import java.time.Instant
import java.util.UUID

data class RefreshToken(
    val id: UUID,
    val userId: UUID,
    val tokenHash: String,
    val expiresAt: Instant,
    val revokedAt: Instant?,
    val createdAt: Instant
) {

    fun isExpired(
        now: Instant
    ): Boolean =
        !expiresAt.isAfter(now)

    fun isRevoked(): Boolean =
        revokedAt != null

    fun isValid(
        now: Instant
    ): Boolean =
        !isRevoked() &&
                !isExpired(now)
}