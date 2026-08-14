package com.blackneko.infrastructure.database

import com.blackneko.application.TransactionRunner

class TransactionRunnerImpl : TransactionRunner {

    override suspend fun <T> transaction(
        block: suspend () -> T
    ): T {

        return block()
    }
}