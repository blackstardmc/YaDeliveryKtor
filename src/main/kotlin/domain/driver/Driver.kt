package com.blackneko.domain.driver

import java.util.UUID
import kotlin.time.Instant

data class Driver(
    val id: UUID,
    val userId: UUID,
    val status: DriverStatus,
    val vehicleType: VehicleType?,
    val vehicleDescription: String?,
    val createdAt: Instant,
    val updatedAt: Instant
)



