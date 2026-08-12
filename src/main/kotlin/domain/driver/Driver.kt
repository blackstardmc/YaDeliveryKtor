package com.blackneko.domain.driver

import java.util.UUID
import kotlin.time.Clock
import kotlin.time.Instant

data class Driver(
    val id: UUID,
    val userId: UUID,
    val status: DriverStatus,
    val vehicleType: VehicleType?,
    val vehicleDescription: String?,
    val createdAt: Instant,
    val updatedAt: Instant
){
    fun isAvailable(): Boolean {
        return status == DriverStatus.AVAILABLE
    }

    fun canAcceptOrder(): Boolean {
        return status == DriverStatus.AVAILABLE
    }

    fun markBusy(): Driver {
        check(status == DriverStatus.AVAILABLE) {
            "Driver is not available"
        }

        return copy(
            status = DriverStatus.BUSY,
            updatedAt = Clock.System.now()
        )
    }

    fun markAvailable(): Driver {
        check(status != DriverStatus.SUSPENDED) {
            "Suspended driver cannot become available"
        }

        return copy(
            status = DriverStatus.AVAILABLE,
            updatedAt = Clock.System.now()
        )
    }
}



