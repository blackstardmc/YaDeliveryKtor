package com.blackneko.config


import com.blackneko.application.TransactionRunner
import com.blackneko.domain.address.AddressRepository
import com.blackneko.domain.auth.RefreshTokenRepository
import com.blackneko.domain.category.CategoryRepository
import com.blackneko.domain.driver.DriverRepository
import com.blackneko.domain.order.OrderItemRepository
import com.blackneko.domain.order.OrderRepository
import com.blackneko.domain.order.OrderStatusHistoryRepository
import com.blackneko.domain.product.ProductRepository
import com.blackneko.domain.restaurant.RestaurantRepository
import com.blackneko.domain.user.UserRepository
import com.blackneko.infrastructure.database.DatabaseConfig
import com.blackneko.infrastructure.database.TransactionRunnerImpl
import com.blackneko.infrastructure.database.repository.AddressRepositoryImpl
import com.blackneko.infrastructure.database.repository.CategoryRepositoryImpl
import com.blackneko.infrastructure.database.repository.DriverRepositoryImpl
import com.blackneko.infrastructure.database.repository.OrderItemRepositoryImpl
import com.blackneko.infrastructure.database.repository.OrderRepositoryImpl
import com.blackneko.infrastructure.database.repository.OrderStatusHistoryRepositoryImpl
import com.blackneko.infrastructure.database.repository.ProductRepositoryImpl
import com.blackneko.infrastructure.database.repository.RefreshTokenRepositoryImpl
import com.blackneko.infrastructure.database.repository.RestaurantRepositoryImpl
import com.blackneko.infrastructure.database.repository.UserRepositoryImpl
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.v1.jdbc.Database
import org.koin.dsl.module

fun infrastructureModule(
    databaseConfig: DatabaseConfig,
    dataSource: HikariDataSource,
    database: Database
) = module {

    single<DatabaseConfig> {
        databaseConfig
    }

    single<HikariDataSource> {
        dataSource
    }

    single<Database> {
        database
    }

    single<TransactionRunner> {
        TransactionRunnerImpl(
            database = get()
        )
    }

    single<UserRepository> {
        UserRepositoryImpl(
            transactionRunner = get()
        )
    }

    single<AddressRepository> {
        AddressRepositoryImpl(
            transactionRunner = get()
        )
    }
    single<RefreshTokenRepository> {
        RefreshTokenRepositoryImpl(
            transactionRunner = get()
        )
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