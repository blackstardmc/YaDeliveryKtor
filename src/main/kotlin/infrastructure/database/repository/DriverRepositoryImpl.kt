package com.blackneko.infrastructure.database.repository

import com.blackneko.application.TransactionRunner
import com.blackneko.domain.driver.Driver
import com.blackneko.domain.driver.DriverRepository
import com.blackneko.domain.driver.DriverStatus
import com.blackneko.infrastructure.database.mapper.toDriver
import com.blackneko.infrastructure.database.table.DriversTable
import com.blackneko.infrastructure.database.table.UsersTable
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import java.util.UUID

class DriverRepositoryImpl(
    transactionRunner: TransactionRunner
) : BaseRepository(transactionRunner),
    DriverRepository {

    override suspend fun findById(
        id: UUID
    ): Driver? =
        transactionRunner.transaction {

            DriversTable
                .selectAll()
                .where {
                    DriversTable.id eq id
                }
                .singleOrNull()
                ?.toDriver()
        }

    override suspend fun findByUserId(
        userId: UUID
    ): Driver? =
        transactionRunner.transaction {

            DriversTable
                .selectAll()
                .where {
                    DriversTable.user eq
                            EntityID(
                                userId,
                                UsersTable
                            )
                }
                .singleOrNull()
                ?.toDriver()
        }

    override suspend fun findAvailable(): List<Driver> =
        transactionRunner.transaction {

            DriversTable
                .selectAll()
                .where {
                    DriversTable.status eq
                            DriverStatus.AVAILABLE.name
                }
                .orderBy(
                    DriversTable.createdAt,
                    SortOrder.ASC
                )
                .map {
                    it.toDriver()
                }
        }

    override suspend fun save(
        driver: Driver
    ): Driver =
        transactionRunner.transaction {

            DriversTable.insert {

                it[id] =
                    driver.id

                it[user] =
                    EntityID(
                        driver.userId,
                        UsersTable
                    )

                it[status] =
                    driver.status.name

                it[vehicleType] =
                    driver.vehicleType?.name

                it[vehicleDescription] =
                    driver.vehicleDescription

                it[createdAt] =
                    driver.createdAt

                it[updatedAt] =
                    driver.updatedAt
            }

            driver
        }

    override suspend fun update(
        driver: Driver
    ): Driver =
        transactionRunner.transaction {

            DriversTable.update(
                where = {
                    DriversTable.id eq
                            driver.id
                }
            ) {

                it[status] =
                    driver.status.name

                it[vehicleType] =
                    driver.vehicleType?.name

                it[vehicleDescription] =
                    driver.vehicleDescription

                it[updatedAt] =
                    driver.updatedAt
            }

            driver
        }

    override suspend fun findByStatus(
        status: DriverStatus
    ): List<Driver> =
        transactionRunner.transaction {

            DriversTable
                .selectAll()
                .where {
                    DriversTable.status eq
                            status.name
                }
                .map {
                    it.toDriver()
                }
        }
}