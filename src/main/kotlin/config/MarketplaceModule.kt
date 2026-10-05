package com.blackneko.config

import com.blackneko.application.marketplace.*
import com.blackneko.domain.order.OrderRequestRepository
import com.blackneko.domain.shared.Money
import com.blackneko.infrastructure.database.repository.OrderRequestRepositoryImpl
import org.koin.dsl.module

fun marketplaceModule(deliveryFeeCents: Long = 0) = module {
    single<OrderRequestRepository> { OrderRequestRepositoryImpl(get()) }
    single { AccessPolicy(get(), get()) }
    single { AddressUseCases(get(), get(), get(), get(), get()) }
    single { CatalogUseCases(get(), get(), get(), get(), get(), get(), get(), get()) }
    single { OrderUseCases(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), Money(deliveryFeeCents)) }
    single { DriverUseCases(get(), get(), get(), get(), get()) }
    single { AdminUseCases(get(), get(), get(), get(), get(), get(), get()) }
}
