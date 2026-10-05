package com.blackneko.infrastructure.database

import com.blackneko.application.EntityLocks
import com.blackneko.application.TransactionRunner
import com.blackneko.infrastructure.database.table.*
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.selectAll
import java.util.UUID

class EntityLocksImpl(private val transactions: TransactionRunner) : EntityLocks {
    override suspend fun user(id: UUID) { transactions.transaction { UsersTable.selectAll().where { UsersTable.id eq id }.forUpdate().toList() } }
    override suspend fun order(id: UUID) { transactions.transaction { OrdersTable.selectAll().where { OrdersTable.id eq id }.forUpdate().toList() } }
    override suspend fun driver(userId: UUID) { transactions.transaction { DriversTable.selectAll().where { DriversTable.user eq userId }.forUpdate().toList() } }
    override suspend fun restaurant(id: UUID) { transactions.transaction { RestaurantsTable.selectAll().where { RestaurantsTable.id eq id }.forUpdate().toList() } }
}
