package com.blackneko.application.marketplace

import com.blackneko.application.*
import com.blackneko.application.exception.*
import com.blackneko.domain.driver.*
import com.blackneko.domain.security.Permission
import java.time.Clock
import java.util.UUID

class DriverUseCases(private val drivers: DriverRepository, private val access: AccessPolicy,
    private val transactions: TransactionRunner, private val locks: EntityLocks, private val clock: Clock) {
    suspend fun me(actor: UUID): Driver {
        access.require(actor, Permission.DRIVER_ORDER_LIST)
        return drivers.findByUserId(actor) ?: throw NotFoundException("Driver not found")
    }
    suspend fun status(actor: UUID, status: DriverStatus): Driver = transactions.transaction {
        locks.driver(actor)
        val driver = me(actor)
        validate(status == DriverStatus.AVAILABLE || status == DriverStatus.OFFLINE, "status", "Choose AVAILABLE or OFFLINE")
        if (driver.status == DriverStatus.BUSY || driver.status == DriverStatus.SUSPENDED) throw ConflictException("Driver status cannot be changed")
        drivers.update(driver.copy(status = status, updatedAt = clock.instant()))
    }
}
