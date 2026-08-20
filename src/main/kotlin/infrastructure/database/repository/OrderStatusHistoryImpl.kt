package com.blackneko.infrastructure.database.repository

import com.blackneko.application.TransactionRunner
import com.blackneko.domain.order.OrderStatusHistory
import com.blackneko.domain.order.OrderStatusHistoryRepository
import com.blackneko.infrastructure.database.mapper.toOrderStatusHistory
import com.blackneko.infrastructure.database.table.OrderStatusHistoryTable
import com.blackneko.infrastructure.database.table.OrdersTable
import com.blackneko.infrastructure.database.table.UsersTable
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import java.util.UUID

class OrderStatusHistoryRepositoryImpl(
    transactionRunner: TransactionRunner
) : BaseRepository(transactionRunner),
    OrderStatusHistoryRepository {

    override suspend fun save(
        history: OrderStatusHistory
    ): OrderStatusHistory =
        transactionRunner.transaction {

            OrderStatusHistoryTable.insert {

                it[id] =
                    history.id

                it[order] =
                    EntityID(
                        history.orderId,
                        OrdersTable
                    )

                it[fromStatus] =
                    history.fromStatus?.name

                it[toStatus] =
                    history.toStatus.name

                it[changedBy] =
                    history.changedBy?.let { userId ->
                        EntityID(
                            userId,
                            UsersTable
                        )
                    }

                it[createdAt] =
                    history.createdAt
            }

            history
        }

    override suspend fun findByOrder(
        orderId: UUID
    ): List<OrderStatusHistory> =
        transactionRunner.transaction {

            OrderStatusHistoryTable
                .selectAll()
                .where {
                    OrderStatusHistoryTable.order eq
                            EntityID(
                                orderId,
                                OrdersTable
                            )
                }
                .orderBy(
                    OrderStatusHistoryTable.createdAt,
                    SortOrder.ASC
                )
                .map {
                    it.toOrderStatusHistory()
                }
        }
}