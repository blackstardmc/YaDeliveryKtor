package com.blackneko.integration

import com.blackneko.domain.address.Address
import com.blackneko.domain.category.Category
import com.blackneko.domain.product.Product
import com.blackneko.domain.product.ProductFilter
import com.blackneko.domain.restaurant.Restaurant
import com.blackneko.domain.restaurant.RestaurantStatus
import com.blackneko.domain.shared.Money
import com.blackneko.domain.user.Role
import com.blackneko.domain.user.User
import com.blackneko.infrastructure.database.repository.AddressRepositoryImpl
import com.blackneko.infrastructure.database.repository.CategoryRepositoryImpl
import com.blackneko.infrastructure.database.repository.ProductRepositoryImpl
import com.blackneko.infrastructure.database.repository.RestaurantRepositoryImpl
import com.blackneko.infrastructure.database.repository.UserRepositoryImpl
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ProductRepositoryIntegrationTest :
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

    @Test
    suspend fun `should save and retrieve product`() {

        val setup =
            createRestaurantSetup()

        val product =
            createProduct(
                restaurantId =
                    setup.restaurant.id,

                categoryId =
                    setup.category.id,

                name =
                    "Pizza Cubana",

                available =
                    true
            )

        productRepository.save(
            product
        )

        val result =
            productRepository.findById(
                product.id
            )

        assertNotNull(
            result
        )

        assertEquals(
            product.id,
            result.id
        )

        assertEquals(
            "Pizza Cubana",
            result.name
        )

        assertEquals(
            1500L,
            result.price.cents
        )
    }

    @Test
    suspend fun `should find only available products`() {

        val setup =
            createRestaurantSetup()

        val pizza =
            createProduct(
                restaurantId =
                    setup.restaurant.id,

                categoryId =
                    setup.category.id,

                name =
                    "Pizza",

                available =
                    true
            )

        val soda =
            createProduct(
                restaurantId =
                    setup.restaurant.id,

                categoryId =
                    setup.category.id,

                name =
                    "Refresco",

                available =
                    false
            )

        productRepository.save(
            pizza
        )

        productRepository.save(
            soda
        )

        val result =
            productRepository
                .findAvailableByRestaurant(
                    setup.restaurant.id
                )

        assertEquals(
            1,
            result.size
        )

        assertEquals(
            pizza.id,
            result.first().id
        )
    }

    @Test
    suspend fun `should search products by restaurant and text`() {

        val setup =
            createRestaurantSetup()

        productRepository.save(
            createProduct(
                restaurantId =
                    setup.restaurant.id,
                categoryId =
                    setup.category.id,
                name =
                    "Pizza Especial",
                available =
                    true
            )
        )

        productRepository.save(
            createProduct(
                restaurantId =
                    setup.restaurant.id,
                categoryId =
                    setup.category.id,
                name =
                    "Hamburguesa",
                available =
                    true
            )
        )

        val result =
            productRepository.search(
                ProductFilter(
                    restaurantId =
                        setup.restaurant.id,

                    search =
                        "Pizza"
                )
            )

        assertEquals(
            1,
            result.size
        )

        assertEquals(
            "Pizza Especial",
            result.first().name
        )
    }

    private suspend fun createRestaurantSetup():
            RestaurantSetup {

        val now =
            Instant.now()

        val owner =
            User(
                id =
                    UUID.randomUUID(),

                email =
                    "restaurant@test.com",

                phone =
                    "+5355550200",

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

        val address =
            Address(
                id =
                    UUID.randomUUID(),

                userId =
                    owner.id,

                label =
                    "Restaurante",

                street =
                    "Calle 10",

                number =
                    "5",

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
            address
        )

        val restaurant =
            Restaurant(
                id =
                    UUID.randomUUID(),

                ownerId =
                    owner.id,

                addressId =
                    address.id,

                name =
                    "Black Neko Food",

                description =
                    "Test restaurant",

                phone =
                    "+5355550201",

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
                    "Comida",

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

        return RestaurantSetup(
            owner =
                owner,

            address =
                address,

            restaurant =
                restaurant,

            category =
                category
        )
    }

    private fun createProduct(
        restaurantId: UUID,
        categoryId: UUID,
        name: String,
        available: Boolean
    ): Product {

        val now =
            Instant.now()

        return Product(
            id =
                UUID.randomUUID(),

            restaurantId =
                restaurantId,

            categoryId =
                categoryId,

            name =
                name,

            description =
                "Producto de prueba",

            price =
                Money(
                    1500L
                ),

            isAvailable =
                available,

            createdAt =
                now,

            updatedAt =
                now
        )
    }

    private data class RestaurantSetup(
        val owner: User,
        val address: Address,
        val restaurant: Restaurant,
        val category: Category
    )
}