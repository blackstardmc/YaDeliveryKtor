package com.blackneko.application.marketplace

import com.blackneko.application.*
import com.blackneko.application.exception.NotFoundException
import com.blackneko.application.exception.ConflictException
import com.blackneko.domain.address.*
import com.blackneko.domain.security.Permission
import com.blackneko.domain.shared.PageRequest
import java.time.Clock
import java.util.UUID

data class AddressCommand(val label: String?, val street: String, val number: String?, val neighborhood: String?,
    val city: String, val province: String?, val reference: String?, val latitude: Double?, val longitude: Double?, val isDefault: Boolean)

class AddressUseCases(private val addresses: AddressRepository, private val access: AccessPolicy,
    private val transactions: TransactionRunner, private val locks: EntityLocks, private val clock: Clock) {
    suspend fun list(actor: UUID, page: PageRequest): List<Address> {
        access.require(actor, Permission.ADDRESS_MANAGE)
        return addresses.findByUser(actor).drop(page.offset).take(page.limit)
    }

    suspend fun get(actor: UUID, id: UUID): Address {
        access.require(actor, Permission.ADDRESS_MANAGE)
        val address = addresses.findById(id) ?: throw NotFoundException("Address not found")
        access.own(actor, address.userId)
        return address
    }

    suspend fun create(actor: UUID, command: AddressCommand): Address = transactions.transaction {
        access.require(actor, Permission.ADDRESS_MANAGE)
        locks.user(actor)
        validate(addresses.findByUser(actor).size < 100, "addresses", "Maximum 100 addresses per user")
        val address = build(actor, UUID.randomUUID(), command)
        addresses.save(address)
    }

    suspend fun update(actor: UUID, id: UUID, command: AddressCommand): Address = transactions.transaction {
        locks.user(actor)
        val original = get(actor, id)
        if (addresses.isUsed(id)) throw ConflictException("Address is in use; create a new address instead")
        addresses.update(build(actor, id, command).copy(createdAt = original.createdAt))
    }

    suspend fun delete(actor: UUID, id: UUID) = transactions.transaction {
        locks.user(actor)
        get(actor, id)
        addresses.delete(id)
    }

    suspend fun setDefault(actor: UUID, id: UUID): Address = transactions.transaction {
        locks.user(actor)
        addresses.update(get(actor, id).copy(isDefault = true, updatedAt = clock.instant()))
    }

    private fun build(actor: UUID, id: UUID, command: AddressCommand): Address {
        validate(command.latitude == null || command.latitude in -90.0..90.0, "latitude", "Invalid latitude")
        validate(command.longitude == null || command.longitude in -180.0..180.0, "longitude", "Invalid longitude")
        val now = clock.instant()
        return Address(id, actor, optionalText(command.label, "label", 100), text(command.street, "street", 255),
            optionalText(command.number, "number", 30), optionalText(command.neighborhood, "neighborhood", 100),
            text(command.city, "city", 100), optionalText(command.province, "province", 100),
            optionalText(command.reference, "reference", 2000), command.latitude, command.longitude, command.isDefault, now, now)
    }
}
