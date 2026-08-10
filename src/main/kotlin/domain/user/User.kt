package com.blackneko.domain.user

import java.util.UUID
import kotlin.time.Instant

data class User(
    val id: UUID,
    val email: String?,
    val phone: String,
    val passwordHash: String,
    val firstName: String,
    val lastName: String,
    val role: UserRole,
    val isActive: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant
)



