package com.blackneko.infrastructure.database

import com.blackneko.application.TransactionRunner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import java.sql.SQLException
import com.blackneko.application.exception.ConflictException
import com.blackneko.application.exception.ValidationException

class TransactionRunnerImpl(
    private val database: Database
) : TransactionRunner {

    override suspend fun <T> transaction(
        block: suspend () -> T
    ): T =
        withContext(
            Dispatchers.IO
        ) {

            try {
            suspendTransaction(
                db = database
            ) {
                block()
            }
            } catch (exception: SQLException) {
                when (exception.sqlState) {
                    "23505", "23503" -> throw ConflictException("Operation conflicts with existing data")
                    "23514", "22001" -> throw ValidationException("Data violates a storage constraint")
                    else -> throw exception
                }
            }
        }
}
