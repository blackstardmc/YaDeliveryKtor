package com.blackneko.domain.category

import java.util.UUID
import kotlin.time.Instant

data class Category(
    val id: UUID,
    val restaurantId: UUID,
    val name: String,
    val description: String?,
    val sortOrder: Int,
    val isActive: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant
)