package com.blackneko.application.marketplace

import com.blackneko.application.*
import com.blackneko.application.exception.*
import com.blackneko.domain.user.*
import com.blackneko.domain.driver.*
import com.blackneko.domain.auth.RefreshTokenRepository
import com.blackneko.domain.security.Permission
import com.blackneko.domain.shared.PageRequest
import java.time.Clock
import java.util.UUID

class AdminUseCases(private val users: UserRepository, private val drivers: DriverRepository,
    private val tokens: RefreshTokenRepository, private val access: AccessPolicy,
    private val transactions: TransactionRunner, private val locks: EntityLocks, private val clock: Clock) {
    suspend fun list(actor: UUID, page: PageRequest): List<User> {
        access.require(actor, Permission.ADMIN_USER_MANAGE)
        return users.findAll(page)
    }
    suspend fun user(actor: UUID, id: UUID, roles: Set<Role>, active: Boolean): User = transactions.transaction {
        access.require(actor, Permission.ADMIN_USER_MANAGE)
        locks.user(id)
        locks.driver(id)
        val user = users.findById(id) ?: throw NotFoundException("User not found")
        if ((!active || Role.DRIVER !in roles) && drivers.findByUserId(id)?.status == DriverStatus.BUSY)
            throw ConflictException("Cannot remove access from a driver with an active order")
        validate(roles.isNotEmpty(), "roles", "At least one role required")
        if (id == actor && (!active || Role.ADMIN !in roles)) throw ConflictException("Cannot remove your own administrative access")
        val updated = users.update(user.copy(roles = roles, isActive = active, updatedAt = clock.instant()))
        tokens.revokeAllByUser(id, clock.instant())
        if (Role.DRIVER in roles && drivers.findByUserId(id) == null) {
            val now = clock.instant()
            drivers.save(Driver(UUID.randomUUID(), id, DriverStatus.OFFLINE, null, null, now, now))
        }
        updated
    }
    suspend fun driverStatus(actor: UUID, userId: UUID, status: DriverStatus): Driver = transactions.transaction {
        access.require(actor, Permission.ADMIN_DRIVER_MANAGE)
        locks.driver(userId)
        val driver = drivers.findByUserId(userId) ?: throw NotFoundException("Driver not found")
        validate(status != DriverStatus.BUSY, "status", "BUSY is managed by order acceptance")
        if (driver.status == DriverStatus.BUSY) throw ConflictException("Cannot change a busy driver")
        drivers.update(driver.copy(status = status, updatedAt = clock.instant()))
    }
}
