package com.blackneko.config

import com.blackneko.application.TransactionRunner
import com.blackneko.infrastructure.database.TransactionRunnerImpl
import com.blackneko.infrastructure.database.providers.ClockProvider
import com.blackneko.infrastructure.database.providers.IdGenerator
import com.blackneko.infrastructure.database.providers.SystemClockProvider
import com.blackneko.infrastructure.database.providers.UUIDGenerator
import org.koin.dsl.module


val repositoryModule = module {

    single<TransactionRunner> {
        TransactionRunnerImpl()
    }

    single<ClockProvider> {
        SystemClockProvider()
    }

    single<IdGenerator> {
        UUIDGenerator()
    }
}