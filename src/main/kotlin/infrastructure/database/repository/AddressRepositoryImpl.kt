package com.blackneko.infrastructure.database.repository

import com.blackneko.application.TransactionRunner
import com.blackneko.domain.address.Address
import com.blackneko.domain.address.AddressRepository
import com.blackneko.infrastructure.database.mapper.toAddress
import com.blackneko.infrastructure.database.table.AddressesTable
import com.blackneko.infrastructure.database.table.UsersTable
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.neq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import java.util.UUID

class AddressRepositoryImpl(
    transactionRunner:
    TransactionRunner
) : BaseRepository(transactionRunner),
    AddressRepository {

    override suspend fun findById(
        id: UUID
    ): Address? =
        transactionRunner.transaction {

            AddressesTable
                .selectAll()
                .where {
                    AddressesTable.id eq id
                }
                .singleOrNull()
                ?.toAddress()
        }

    override suspend fun findByUser(
        userId: UUID
    ): List<Address> =
        transactionRunner.transaction {

            AddressesTable
                .selectAll()
                .where {
                    AddressesTable.user eq
                            EntityID(
                                userId,
                                UsersTable
                            )
                }
                .map {
                    it.toAddress()
                }
        }

    override suspend fun findDefaultByUser(userId: UUID): Address? {
        TODO("Not yet implemented")
    }

    override suspend fun save(
        address: Address
    ): Address =
        transactionRunner.transaction {

            if (address.isDefault) {
                clearDefaultAddress(
                    userId = address.userId
                )
            }

            AddressesTable.insert {

                it[id] = address.id

                it[user] =
                    EntityID(
                        address.userId,
                        UsersTable
                    )

                it[label] =
                    address.label

                it[street] =
                    address.street

                it[number] =
                    address.number

                it[neighborhood] =
                    address.neighborhood

                it[city] =
                    address.city

                it[province] =
                    address.province

                it[referenceText] =
                    address.reference

                it[latitude] =
                    address.latitude

                it[longitude] =
                    address.longitude

                it[isDefault] =
                    address.isDefault

                it[createdAt] =
                    address.createdAt

                it[updatedAt] =
                    address.updatedAt
            }

            address
        }

    override suspend fun update(
        address: Address
    ): Address =
        transactionRunner.transaction {

            if (address.isDefault) {
                clearDefaultAddress(
                    userId = address.userId,
                    exceptId = address.id
                )
            }

            AddressesTable.update(
                where = {
                    AddressesTable.id eq
                            address.id
                }
            ) {

                it[user] =
                    EntityID(
                        address.userId,
                        UsersTable
                    )

                it[label] =
                    address.label

                it[street] =
                    address.street

                it[number] =
                    address.number

                it[neighborhood] =
                    address.neighborhood

                it[city] =
                    address.city

                it[province] =
                    address.province

                it[referenceText] =
                    address.reference

                it[latitude] =
                    address.latitude

                it[longitude] =
                    address.longitude

                it[isDefault] =
                    address.isDefault

                it[updatedAt] =
                    address.updatedAt
            }

            address
        }

    override suspend fun delete(
        id: UUID
    ) {
        transactionRunner.transaction {

            AddressesTable.deleteWhere {
                AddressesTable.id eq id
            }
        }
    }

    private fun clearDefaultAddress(
        userId: UUID,
        exceptId: UUID? = null
    ) {

        val userEntityId =
            EntityID(
                userId,
                UsersTable
            )

        if (exceptId == null) {

            AddressesTable.update(
                where = {
                    (AddressesTable.user eq userEntityId) and
                            (AddressesTable.isDefault eq true)
                }
            ) {
                it[isDefault] = false
            }

        } else {

            AddressesTable.update(
                where = {
                    (AddressesTable.user eq userEntityId) and
                            (AddressesTable.isDefault eq true) and
                            (AddressesTable.id neq exceptId)
                }
            ) {
                it[isDefault] = false
            }
        }
    }
}