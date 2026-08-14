package com.blackneko.application

interface TransactionRunner {

    suspend fun <T> transaction(
        block: suspend () -> T
    ): T
}