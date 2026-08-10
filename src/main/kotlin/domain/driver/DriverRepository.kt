package com.blackneko.domain.driver

import java.util.UUID

interface DriverRepository {
    suspend fun findById(id: UUID): Driver?

    suspend fun findByUser(
        userID: UUID
    ): List<Driver>

    suspend fun save(driver: Driver): Driver

    suspend fun update(driver: Driver): Driver

    suspend fun delete(id: UUID)
}
