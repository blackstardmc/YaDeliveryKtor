package com.blackneko.infrastructure.database.repository

import com.blackneko.application.TransactionRunner
import com.blackneko.domain.order.Order
import com.blackneko.domain.order.OrderRepository
import com.blackneko.domain.order.OrderStatus
import com.blackneko.infrastructure.database.mapper.toOrder
import com.blackneko.infrastructure.database.table.AddressesTable
import com.blackneko.infrastructure.database.table.DriversTable
import com.blackneko.infrastructure.database.table.OrdersTable
import com.blackneko.infrastructure.database.table.RestaurantsTable
import com.blackneko.infrastructure.database.table.UsersTable
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.isNull
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import java.util.UUID
import com.blackneko.domain.shared.PageRequest

class OrderRepositoryImpl(
    transactionRunner: TransactionRunner
) : BaseRepository(transactionRunner),
    OrderRepository {

    override suspend fun findById(
        id: UUID
    ): Order? =
        transactionRunner.transaction {

            OrdersTable
                .selectAll()
                .where {
                    OrdersTable.id eq id
                }
                .singleOrNull()
                ?.toOrder()
        }

    override suspend fun findByCustomer(
        customerId: UUID, page: PageRequest): List<Order> =
        transactionRunner.transaction {

            OrdersTable
                .selectAll()
                .where {
                    OrdersTable.customer eq
                            EntityID(
                                customerId,
                                UsersTable
                            )
                }
                .orderBy(
                    OrdersTable.createdAt,
                    SortOrder.DESC
                )
                .limit(page.limit).offset(page.offset.toLong())
                .map {
                    it.toOrder()
                }
        }

    override suspend fun findByRestaurant(
        restaurantId: UUID, page: PageRequest): List<Order> =
        transactionRunner.transaction {

            OrdersTable
                .selectAll()
                .where {
                    OrdersTable.restaurant eq
                            EntityID(
                                restaurantId,
                                RestaurantsTable
                            )
                }
                .orderBy(
                    OrdersTable.createdAt,
                    SortOrder.DESC
                )
                .limit(page.limit).offset(page.offset.toLong())
                .map {
                    it.toOrder()
                }
        }

    override suspend fun findByDriver(
        driverId: UUID, page: PageRequest): List<Order> =
        transactionRunner.transaction {

            OrdersTable
                .selectAll()
                .where {
                    OrdersTable.driver eq
                            EntityID(
                                driverId,
                                DriversTable
                            )
                }
                .orderBy(
                    OrdersTable.createdAt,
                    SortOrder.DESC
                )
                .limit(page.limit).offset(page.offset.toLong())
                .map {
                    it.toOrder()
                }
        }

    override suspend fun findAvailableForDrivers(page: PageRequest):
            List<Order> =
        transactionRunner.transaction {

            OrdersTable
                .selectAll()
                .where {

                    (OrdersTable.status eq
                            OrderStatus.READY.name) and

                            OrdersTable.driver.isNull()
                }
                .orderBy(
                    OrdersTable.createdAt,
                    SortOrder.ASC
                )
                .limit(page.limit).offset(page.offset.toLong())
                .map {
                    it.toOrder()
                }
        }

    override suspend fun save(
        order: Order
    ): Order =
        transactionRunner.transaction {

            OrdersTable.insert {

                it[id] =
                    order.id

                it[customer] =
                    EntityID(
                        order.customerId,
                        UsersTable
                    )

                it[restaurant] =
                    EntityID(
                        order.restaurantId,
                        RestaurantsTable
                    )

                it[driver] =
                    order.driverId?.let { driverId ->
                        EntityID(
                            driverId,
                            DriversTable
                        )
                    }

                it[deliveryAddress] =
                    EntityID(
                        order.deliveryAddressId,
                        AddressesTable
                    )

                it[status] =
                    order.status.name

                it[subtotal] =
                    order.subtotal.cents

                it[deliveryFee] =
                    order.deliveryFee.cents

                it[total] =
                    order.total.cents

                it[notes] =
                    order.notes

                it[createdAt] =
                    order.createdAt

                it[confirmedAt] =
                    order.confirmedAt

                it[preparingAt] =
                    order.preparingAt

                it[readyAt] =
                    order.readyAt

                it[pickedUpAt] =
                    order.pickedUpAt

                it[deliveredAt] =
                    order.deliveredAt

                it[cancelledAt] =
                    order.cancelledAt

                it[updatedAt] =
                    order.updatedAt
            }

            order
        }

    override suspend fun update(
        order: Order
    ): Order =
        transactionRunner.transaction {

            OrdersTable.update(
                where = {
                    OrdersTable.id eq order.id
                }
            ) {

                it[driver] =
                    order.driverId?.let { driverId ->
                        EntityID(
                            driverId,
                            DriversTable
                        )
                    }

                it[status] =
                    order.status.name

                it[subtotal] =
                    order.subtotal.cents

                it[deliveryFee] =
                    order.deliveryFee.cents

                it[total] =
                    order.total.cents

                it[notes] =
                    order.notes

                it[confirmedAt] =
                    order.confirmedAt

                it[preparingAt] =
                    order.preparingAt

                it[readyAt] =
                    order.readyAt

                it[pickedUpAt] =
                    order.pickedUpAt

                it[deliveredAt] =
                    order.deliveredAt

                it[cancelledAt] =
                    order.cancelledAt

                it[updatedAt] =
                    order.updatedAt
            }

            order
        }
}
