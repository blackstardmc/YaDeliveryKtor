package com.blackneko.domain.restaurant

import java.util.UUID
import kotlin.time.Instant

data class Restaurant(
    val id: UUID,
    val ownerId: UUID,
    val name: String,
    val description: String?,
    val phone: String?,
    val addressId: UUID,
    val status: RestaurantStatus,
    val createdAt: Instant,
    val updatedAt: Instant
)

