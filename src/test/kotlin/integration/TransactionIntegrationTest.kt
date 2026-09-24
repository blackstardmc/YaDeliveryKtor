package com.blackneko.integration

import com.blackneko.domain.address.Address
import com.blackneko.domain.category.Category
import com.blackneko.domain.order.Order
import com.blackneko.domain.order.OrderItem
import com.blackneko.domain.order.OrderStatus
import com.blackneko.domain.order.OrderStatusHistory
import com.blackneko.domain.product.Product
import com.blackneko.domain.restaurant.Restaurant
import com.blackneko.domain.restaurant.RestaurantStatus
import com.blackneko.domain.shared.Money
import com.blackneko.domain.user.Role
import com.blackneko.domain.user.User
import com.blackneko.infrastructure.database.repository.AddressRepositoryImpl
import com.blackneko.infrastructure.database.repository.CategoryRepositoryImpl
import com.blackneko.infrastructure.database.repository.OrderItemRepositoryImpl
import com.blackneko.infrastructure.database.repository.OrderRepositoryImpl
import com.blackneko.infrastructure.database.repository.OrderStatusHistoryRepositoryImpl
import com.blackneko.infrastructure.database.repository.ProductRepositoryImpl
import com.blackneko.infrastructure.database.repository.RestaurantRepositoryImpl
import com.blackneko.infrastructure.database.repository.UserRepositoryImpl
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class TransactionIntegrationTest :
    DatabaseIntegrationTest() {

    private val userRepository by lazy {
        UserRepositoryImpl(
            transactionRunner
        )
    }

    private val addressRepository by lazy {
        AddressRepositoryImpl(
            transactionRunner
        )
    }

    private val restaurantRepository by lazy {
        RestaurantRepositoryImpl(
            transactionRunner
        )
    }

    private val categoryRepository by lazy {
        CategoryRepositoryImpl(
            transactionRunner
        )
    }

    private val productRepository by lazy {
        ProductRepositoryImpl(
            transactionRunner
        )
    }

    private val orderRepository by lazy {
        OrderRepositoryImpl(
            transactionRunner
        )
    }

    private val orderItemRepository by lazy {
        OrderItemRepositoryImpl(
            transactionRunner
        )
    }

    private val historyRepository by lazy {
        OrderStatusHistoryRepositoryImpl(
            transactionRunner
        )
    }

    @Test
    fun `should rollback order items and history when transaction fails`() =
        runBlocking {

            val setup =
                createSetup()

            val now =
                Instant.now()

            val order =
                Order(
                    id =
                        UUID.randomUUID(),

                    customerId =
                        setup.customer.id,

                    restaurantId =
                        setup.restaurant.id,

                    driverId =
                        null,

                    deliveryAddressId =
                        setup.customerAddress.id,

                    status =
                        OrderStatus.RECEIVED,

                    subtotal =
                        Money(
                            3000L
                        ),

                    deliveryFee =
                        Money(
                            500L
                        ),

                    total =
                        Money(
                            3500L
                        ),

                    notes =
                        null,

                    createdAt =
                        now,

                    confirmedAt =
                        null,

                    preparingAt =
                        null,

                    readyAt =
                        null,

                    pickedUpAt =
                        null,

                    deliveredAt =
                        null,

                    cancelledAt =
                        null,

                    updatedAt =
                        now
                )

            val item =
                OrderItem(
                    id =
                        UUID.randomUUID(),

                    orderId =
                        order.id,

                    productId =
                        setup.product.id,

                    productName =
                        setup.product.name,

                    unitPrice =
                        setup.product.price,

                    quantity =
                        2,

                    subtotal =
                        Money(
                            setup.product.price.cents * 2
                        )
                )

            val history =
                OrderStatusHistory(
                    id =
                        UUID.randomUUID(),

                    orderId =
                        order.id,

                    fromStatus =
                        null,

                    toStatus =
                        OrderStatus.RECEIVED,

                    changedBy =
                        setup.customer.id,

                    createdAt =
                        now
                )

            assertFailsWith<IllegalStateException> {

                transactionRunner.transaction {

                    orderRepository.save(
                        order
                    )

                    orderItemRepository.saveAll(
                        listOf(
                            item
                        )
                    )

                    historyRepository.save(
                        history
                    )

                    throw IllegalStateException(
                        "Simulated failure"
                    )
                }
            }

            val persistedOrder =
                orderRepository.findById(
                    order.id
                )

            val persistedItems =
                orderItemRepository.findByOrder(
                    order.id
                )

            val persistedHistory =
                historyRepository.findByOrder(
                    order.id
                )

            assertNull(
                persistedOrder
            )

            kotlin.test.assertTrue(
                persistedItems.isEmpty()
            )

            kotlin.test.assertTrue(
                persistedHistory.isEmpty()
            )
        }

    private suspend fun createSetup():
            TransactionSetup {

        val now =
            Instant.now()

        val customer =
            User(
                id =
                    UUID.randomUUID(),

                email =
                    "transaction.customer@test.com",

                phone =
                    "+5355550400",

                passwordHash =
                    "\$2a\$12\$fakeHash",

                firstName =
                    "Transaction",

                lastName =
                    "Customer",

                roles =
                    setOf(
                        Role.CUSTOMER
                    ),

                isActive =
                    true,

                createdAt =
                    now,

                updatedAt =
                    now
            )

        userRepository.save(
            customer
        )

        val customerAddress =
            Address(
                id =
                    UUID.randomUUID(),

                userId =
                    customer.id,

                label =
                    "Casa",

                street =
                    "Calle Customer",

                number =
                    "1",

                neighborhood =
                    "Centro",

                city =
                    "La Habana",

                province =
                    "La Habana",

                reference =
                    null,

                latitude =
                    null,

                longitude =
                    null,

                isDefault =
                    true,

                createdAt =
                    now,

                updatedAt =
                    now
            )

        addressRepository.save(
            customerAddress
        )

        val owner =
            User(
                id =
                    UUID.randomUUID(),

                email =
                    "transaction.owner@test.com",

                phone =
                    "+5355550401",

                passwordHash =
                    "\$2a\$12\$fakeHash",

                firstName =
                    "Transaction",

                lastName =
                    "Owner",

                roles =
                    setOf(
                        Role.RESTAURANT
                    ),

                isActive =
                    true,

                createdAt =
                    now,

                updatedAt =
                    now
            )

        userRepository.save(
            owner
        )

        val restaurantAddress =
            Address(
                id =
                    UUID.randomUUID(),

                userId =
                    owner.id,

                label =
                    "Restaurant",

                street =
                    "Calle Restaurant",

                number =
                    "2",

                neighborhood =
                    "Centro",

                city =
                    "La Habana",

                province =
                    "La Habana",

                reference =
                    null,

                latitude =
                    null,

                longitude =
                    null,

                isDefault =
                    true,

                createdAt =
                    now,

                updatedAt =
                    now
            )

        addressRepository.save(
            restaurantAddress
        )

        val restaurant =
            Restaurant(
                id =
                    UUID.randomUUID(),

                ownerId =
                    owner.id,

                addressId =
                    restaurantAddress.id,

                name =
                    "Transaction Restaurant",

                description =
                    null,

                phone =
                    "+5355550402",

                status =
                    RestaurantStatus.ACTIVE,

                createdAt =
                    now,

                updatedAt =
                    now
            )

        restaurantRepository.save(
            restaurant
        )

        val category =
            Category(
                id =
                    UUID.randomUUID(),

                restaurantId =
                    restaurant.id,

                name =
                    "Transaction Category",

                description =
                    null,

                sortOrder =
                    0,

                isActive =
                    true,

                createdAt =
                    now,

                updatedAt =
                    now
            )

        categoryRepository.save(
            category
        )

        val product =
            Product(
                id =
                    UUID.randomUUID(),

                restaurantId =
                    restaurant.id,

                categoryId =
                    category.id,

                name =
                    "Transaction Product",

                description =
                    null,

                price =
                    Money(
                        1500L
                    ),

                isAvailable =
                    true,

                createdAt =
                    now,

                updatedAt =
                    now
            )

        productRepository.save(
            product
        )

        return TransactionSetup(
            customer =
                customer,

            customerAddress =
                customerAddress,

            restaurant =
                restaurant,

            product =
                product
        )
    }

    private data class TransactionSetup(
        val customer: User,
        val customerAddress: Address,
        val restaurant: Restaurant,
        val product: Product
    )
}