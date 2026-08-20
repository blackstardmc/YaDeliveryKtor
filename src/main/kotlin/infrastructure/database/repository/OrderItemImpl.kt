package com.blackneko.infrastructure.database.repository

import com.blackneko.application.TransactionRunner
import com.blackneko.domain.order.OrderItem
import com.blackneko.domain.order.OrderItemRepository
import com.blackneko.infrastructure.database.mapper.toOrderItem
import com.blackneko.infrastructure.database.table.OrderItemsTable
import com.blackneko.infrastructure.database.table.OrdersTable
import com.blackneko.infrastructure.database.table.ProductsTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.batchInsert
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import java.util.UUID

class OrderItemRepositoryImpl(
    transactionRunner: TransactionRunner
) : BaseRepository(transactionRunner),
    OrderItemRepository {

    override suspend fun findByOrder(
        orderId: UUID
    ): List<OrderItem> =
        transactionRunner.transaction {

            OrderItemsTable
                .selectAll()
                .where {
                    OrderItemsTable.order eq
                            EntityID(
                                orderId,
                                OrdersTable
                            )
                }
                .map {
                    it.toOrderItem()
                }
        }

    override suspend fun save(
        item: OrderItem
    ): OrderItem =
        transactionRunner.transaction {

            OrderItemsTable.insert {

                it[id] =
                    item.id

                it[order] =
                    EntityID(
                        item.orderId,
                        OrdersTable
                    )

                it[product] =
                    EntityID(
                        item.productId,
                        ProductsTable
                    )

                it[productName] =
                    item.productName

                it[unitPrice] =
                    item.unitPrice.cents

                it[quantity] =
                    item.quantity

                it[subtotal] =
                    item.subtotal.cents
            }

            item
        }

    override suspend fun saveAll(
        items: List<OrderItem>
    ) {

        if (items.isEmpty()) {
            return
        }

        transactionRunner.transaction {

            OrderItemsTable.batchInsert(
                items
            ) { item ->

                this[OrderItemsTable.id] =
                    item.id

                this[OrderItemsTable.order] =
                    EntityID(
                        item.orderId,
                        OrdersTable
                    )

                this[OrderItemsTable.product] =
                    EntityID(
                        item.productId,
                        ProductsTable
                    )

                this[OrderItemsTable.productName] =
                    item.productName

                this[OrderItemsTable.unitPrice] =
                    item.unitPrice.cents

                this[OrderItemsTable.quantity] =
                    item.quantity

                this[OrderItemsTable.subtotal] =
                    item.subtotal.cents
            }
        }
    }
}