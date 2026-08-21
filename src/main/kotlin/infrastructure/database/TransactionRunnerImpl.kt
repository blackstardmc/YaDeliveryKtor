package com.blackneko.infrastructure.database

import com.blackneko.application.TransactionRunner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction

class TransactionRunnerImpl(
    private val database: Database
) : TransactionRunner {

    override suspend fun <T> transaction(
        block: suspend () -> T
    ): T =
        withContext(
            Dispatchers.IO
        ) {

            suspendTransaction(
                db = database
            ) {
                block()
            }
        }
}