package com.blackneko.integration

import com.blackneko.application.marketplace.*
import com.blackneko.domain.address.Address
import com.blackneko.domain.category.Category
import com.blackneko.domain.product.Product
import com.blackneko.domain.restaurant.*
import com.blackneko.domain.user.*
import com.blackneko.domain.driver.*
import com.blackneko.domain.order.*
import com.blackneko.domain.shared.Money
import com.blackneko.infrastructure.database.EntityLocksImpl
import com.blackneko.infrastructure.database.repository.*
import java.time.Clock
import java.util.UUID

abstract class MarketplaceFixture : DatabaseIntegrationTest() {
    protected val users by lazy { UserRepositoryImpl(transactionRunner) }
    protected val addresses by lazy { AddressRepositoryImpl(transactionRunner) }
    protected val restaurants by lazy { RestaurantRepositoryImpl(transactionRunner) }
    protected val categories by lazy { CategoryRepositoryImpl(transactionRunner) }
    protected val products by lazy { ProductRepositoryImpl(transactionRunner) }
    protected val drivers by lazy { DriverRepositoryImpl(transactionRunner) }
    protected val orders by lazy { OrderRepositoryImpl(transactionRunner) }
    protected val items by lazy { OrderItemRepositoryImpl(transactionRunner) }
    protected val history by lazy { OrderStatusHistoryRepositoryImpl(transactionRunner) }
    protected val requests by lazy { OrderRequestRepositoryImpl(transactionRunner) }
    protected val access by lazy { AccessPolicy(users, restaurants) }
    protected val locks by lazy { EntityLocksImpl(transactionRunner) }
    protected val clock = Clock.systemUTC()
    protected val catalog by lazy { CatalogUseCases(restaurants, categories, products, addresses, access, transactionRunner, locks, clock) }
    protected val addressCases by lazy { AddressUseCases(addresses, access, transactionRunner, locks, clock) }
    protected fun orderCases(historyRepository: OrderStatusHistoryRepository = history) =
        OrderUseCases(orders, items, historyRepository, requests, restaurants, addresses, products, drivers, access, transactionRunner, locks, clock, Money(500))

    protected data class Seed(val customer: User, val otherCustomer: User, val owner: User, val otherOwner: User,
        val driverA: Driver, val driverB: Driver, val address: Address, val otherAddress: Address,
        val restaurant: Restaurant, val otherRestaurant: Restaurant, val category: Category, val product: Product, val otherProduct: Product) {
        fun command() = CreateOrderCommand(restaurant.id, address.id, listOf(OrderLineCommand(product.id, 2)), "Please call")
    }

    protected suspend fun seed(): Seed {
        val now = clock.instant()
        suspend fun user(name: String, role: Role) = users.save(User(UUID.randomUUID(), "$name@test.com", name,
            "hash", name, "Test", setOf(role), true, now, now))
        val customer = user("customer", Role.CUSTOMER)
        val otherCustomer = user("otherCustomer", Role.CUSTOMER)
        val owner = user("owner", Role.RESTAURANT)
        val otherOwner = user("otherOwner", Role.RESTAURANT)
        val driverUserA = user("driverA", Role.DRIVER)
        val driverUserB = user("driverB", Role.DRIVER)
        suspend fun address(user: User) = addresses.save(Address(UUID.randomUUID(), user.id, "Home", "Calle 1", "1",
            null, "Habana", "Habana", null, null, null, true, now, now))
        val address = address(customer)
        val otherAddress = address(otherCustomer)
        suspend fun restaurant(user: User): Restaurant {
            val restaurantAddress = address(user)
            return restaurants.save(Restaurant(UUID.randomUUID(), user.id, user.firstName, null, null,
                restaurantAddress.id, RestaurantStatus.ACTIVE, now, now))
        }
        val restaurant = restaurant(owner)
        val otherRestaurant = restaurant(otherOwner)
        val category = categories.save(Category(UUID.randomUUID(), restaurant.id, "Main", null, 0, true, now, now))
        val product = products.save(Product(UUID.randomUUID(), restaurant.id, category.id, "Pizza", null, Money(1500), true, now, now))
        val otherProduct = products.save(Product(UUID.randomUUID(), otherRestaurant.id, null, "Other", null, Money(2000), true, now, now))
        suspend fun driver(user: User) = drivers.save(Driver(UUID.randomUUID(), user.id, DriverStatus.AVAILABLE,
            VehicleType.BICYCLE, null, now, now))
        return Seed(customer, otherCustomer, owner, otherOwner, driver(driverUserA), driver(driverUserB),
            address, otherAddress, restaurant, otherRestaurant, category, product, otherProduct)
    }

    protected suspend fun ready(seed: Seed): OrderDetail {
        val useCases = orderCases()
        var result = useCases.create(seed.customer.id, seed.command(), null)
        listOf(OrderStatus.CONFIRMED, OrderStatus.PREPARING, OrderStatus.READY).forEach {
            result = useCases.transition(seed.owner.id, result.order.id, it)
        }
        return result
    }
}
