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
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class OrderRepositoryIntegrationTest :
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
    suspend fun `should save and retrieve complete order`() {

        val setup =
            createOrderSetup()

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
                    "Sin cebolla",

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

        assertNotNull(
            persistedOrder
        )

        assertEquals(
            OrderStatus.RECEIVED,
            persistedOrder.status
        )

        assertEquals(
            3000L,
            persistedOrder.subtotal.cents
        )

        assertEquals(
            500L,
            persistedOrder.deliveryFee.cents
        )

        assertEquals(
            3500L,
            persistedOrder.total.cents
        )

        assertNull(
            persistedOrder.driverId
        )

        assertEquals(
            1,
            persistedItems.size
        )

        assertEquals(
            setup.product.id,
            persistedItems.first().productId
        )

        assertEquals(
            2,
            persistedItems.first().quantity
        )

        assertEquals(
            3000L,
            persistedItems.first().subtotal.cents
        )

        assertEquals(
            1,
            persistedHistory.size
        )

        assertNull(
            persistedHistory.first().fromStatus
        )

        assertEquals(
            OrderStatus.RECEIVED,
            persistedHistory.first().toStatus
        )
    }

    @Test
    suspend fun `should find orders by customer`() {

        val setup =
            createOrderSetup()

        val order =
            createOrder(
                setup
            )

        orderRepository.save(
            order
        )

        val result =
            orderRepository.findByCustomer(
                setup.customer.id
            )

        assertEquals(
            1,
            result.size
        )

        assertEquals(
            order.id,
            result.first().id
        )
    }

    @Test
    suspend fun `should find orders by restaurant`() {

        val setup =
            createOrderSetup()

        val order =
            createOrder(
                setup
            )

        orderRepository.save(
            order
        )

        val result =
            orderRepository.findByRestaurant(
                setup.restaurant.id
            )

        assertEquals(
            1,
            result.size
        )

        assertEquals(
            order.id,
            result.first().id
        )
    }

    private suspend fun createOrderSetup():
            OrderSetup {

        val now =
            Instant.now()

        val customer =
            User(
                id =
                    UUID.randomUUID(),

                email =
                    "customer@test.com",

                phone =
                    "+5355550300",

                passwordHash =
                    "\$2a\$12\$fakeHash",

                firstName =
                    "Customer",

                lastName =
                    "Test",

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
                    "Calle Cliente",

                number =
                    "10",

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
                    "owner@test.com",

                phone =
                    "+5355550301",

                passwordHash =
                    "\$2a\$12\$fakeHash",

                firstName =
                    "Restaurant",

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
                    "Restaurante",

                street =
                    "Calle Restaurante",

                number =
                    "20",

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
                    "Restaurant Test",

                description =
                    "Restaurant for integration tests",

                phone =
                    "+5355550302",

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
                    "Pizzas",

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
                    "Pizza Familiar",

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

        return OrderSetup(
            customer =
                customer,

            customerAddress =
                customerAddress,

            owner =
                owner,

            restaurantAddress =
                restaurantAddress,

            restaurant =
                restaurant,

            category =
                category,

            product =
                product
        )
    }

    private fun createOrder(
        setup: OrderSetup
    ): Order {

        val now =
            Instant.now()

        return Order(
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
                    1500L
                ),

            deliveryFee =
                Money(
                    500L
                ),

            total =
                Money(
                    2000L
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
    }

    private data class OrderSetup(
        val customer: User,
        val customerAddress: Address,
        val owner: User,
        val restaurantAddress: Address,
        val restaurant: Restaurant,
        val category: Category,
        val product: Product
    )
}