package com.blackneko.infrastructure.database.repository

import com.blackneko.application.TransactionRunner
import com.blackneko.domain.order.*
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.*
import java.util.UUID
import com.blackneko.infrastructure.database.table.UsersTable
import com.blackneko.infrastructure.database.table.OrdersTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID

private object OrderRequestsTable : Table("order_requests") {
    val userId = reference("user_id", UsersTable)
    val key = varchar("request_key", 100)
    val fingerprint = varchar("fingerprint", 64)
    val orderId = reference("order_id", OrdersTable)
    override val primaryKey = PrimaryKey(userId, key)
}

class OrderRequestRepositoryImpl(private val transactions: TransactionRunner) : OrderRequestRepository {
    override suspend fun find(userId: UUID, key: String): OrderRequest? = transactions.transaction {
        OrderRequestsTable.selectAll().where { (OrderRequestsTable.userId eq userId) and (OrderRequestsTable.key eq key) }
            .singleOrNull()?.let { OrderRequest(it[OrderRequestsTable.userId].value, it[OrderRequestsTable.key], it[OrderRequestsTable.fingerprint], it[OrderRequestsTable.orderId].value) }
    }
    override suspend fun save(request: OrderRequest) { transactions.transaction {
        OrderRequestsTable.insert {
            it[userId] = EntityID(request.userId, UsersTable)
            it[key] = request.key
            it[fingerprint] = request.fingerprint
            it[orderId] = EntityID(request.orderId, OrdersTable)
        }
    } }
}
