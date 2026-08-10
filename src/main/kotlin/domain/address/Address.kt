package com.blackneko.domain.address

import java.util.UUID
import kotlin.time.Instant

data class Address(
    val id: UUID,
    val userId: UUID,
    val label: String?,
    val street: String,
    val number: String?,
    val neighborhood: String?,
    val city: String,
    val province: String?,
    val reference: String?,
    val latitude: Double?,
    val longitude: Double?,
    val isDefault: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant
)