package com.blackneko.config


import com.blackneko.application.TransactionRunner
import com.blackneko.domain.category.CategoryRepository
import com.blackneko.domain.restaurant.RestaurantRepository
import com.blackneko.infrastructure.database.TransactionRunnerImpl
import com.blackneko.infrastructure.database.repository.CategoryRepositoryImpl
import com.blackneko.infrastructure.database.repository.RestaurantRepositoryImpl
import org.koin.dsl.module

val applicationModule = module {
    single<TransactionRunner> {
        TransactionRunnerImpl()
    }

    single<RestaurantRepository> {
        RestaurantRepositoryImpl(
            transactionRunner = get()
        )
    }

    single<CategoryRepository> {
        CategoryRepositoryImpl(
            transactionRunner = get()
        )
    }
}