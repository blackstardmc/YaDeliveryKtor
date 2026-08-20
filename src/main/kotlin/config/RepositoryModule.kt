package com.blackneko.config

import com.blackneko.application.TransactionRunner
import com.blackneko.domain.address.AddressRepository
import com.blackneko.domain.category.CategoryRepository
import com.blackneko.domain.driver.DriverRepository
import com.blackneko.domain.order.OrderItemRepository
import com.blackneko.domain.order.OrderRepository
import com.blackneko.domain.order.OrderStatusHistoryRepository
import com.blackneko.domain.product.ProductRepository
import com.blackneko.domain.restaurant.RestaurantRepository
import com.blackneko.infrastructure.database.TransactionRunnerImpl
import com.blackneko.infrastructure.database.providers.ClockProvider
import com.blackneko.infrastructure.database.providers.IdGenerator
import com.blackneko.infrastructure.database.providers.SystemClockProvider
import com.blackneko.infrastructure.database.providers.UUIDGenerator
import com.blackneko.infrastructure.database.repository.AddressRepositoryImpl
import com.blackneko.infrastructure.database.repository.CategoryRepositoryImpl
import com.blackneko.infrastructure.database.repository.DriverRepositoryImpl
import com.blackneko.infrastructure.database.repository.OrderItemRepositoryImpl
import com.blackneko.infrastructure.database.repository.OrderRepositoryImpl
import com.blackneko.infrastructure.database.repository.OrderStatusHistoryRepositoryImpl
import com.blackneko.infrastructure.database.repository.ProductRepositoryImpl
import com.blackneko.infrastructure.database.repository.RestaurantRepositoryImpl
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

    single<ProductRepository> {
        ProductRepositoryImpl(
            transactionRunner = get()
        )
    }

    single<AddressRepository> {
        AddressRepositoryImpl(
            transactionRunner = get()
        )
    }

    single<DriverRepository> {
        DriverRepositoryImpl(
            transactionRunner = get()
        )
    }

    single<OrderRepository> {
        OrderRepositoryImpl(
            transactionRunner = get()
        )
    }

    single<OrderItemRepository> {
        OrderItemRepositoryImpl(
            transactionRunner = get()
        )
    }

    single<OrderStatusHistoryRepository> {
        OrderStatusHistoryRepositoryImpl(
            transactionRunner = get()
        )
    }
}