package com.blackneko.domain.driver

import java.util.UUID

interface DriverRepository {
    suspend fun findById(
        id: UUID
    ): Driver?

    suspend fun findByUserId(
        userId: UUID
    ): Driver?

    suspend fun findAvailable(): List<Driver>

    suspend fun findByStatus(
        status: DriverStatus
    ): List<Driver>


    suspend fun save(
        driver: Driver
    ): Driver

    suspend fun update(
        driver: Driver
    ): Driver
}
