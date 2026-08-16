package com.blackneko.infrastructure.database

import com.blackneko.application.TransactionRunner
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.v1.jdbc.transactions.experimental.newSuspendedTransaction

class TransactionRunnerImpl : TransactionRunner {

    override suspend fun <T> transaction(
        block: suspend () -> T
    ): T {

        return newSuspendedTransaction(
            Dispatchers.IO
        ) {
            block()
        }
    }
}