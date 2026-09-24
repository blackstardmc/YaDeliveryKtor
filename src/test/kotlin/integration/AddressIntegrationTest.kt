package com.blackneko.integration

import com.blackneko.domain.address.Address
import com.blackneko.domain.user.Role
import com.blackneko.domain.user.User
import com.blackneko.infrastructure.database.repository.AddressRepositoryImpl
import com.blackneko.infrastructure.database.repository.UserRepositoryImpl
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AddressRepositoryIntegrationTest :
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

    @Test
    suspend fun `should save and retrieve address`() {

        val user =
            createUser()

        userRepository.save(
            user
        )

        val address =
            createAddress(
                userId = user.id,
                isDefault = true
            )

        addressRepository.save(
            address
        )

        val result =
            addressRepository.findById(
                address.id
            )

        assertNotNull(
            result
        )

        assertEquals(
            address.id,
            result.id
        )

        assertEquals(
            user.id,
            result.userId
        )

        assertTrue(
            result.isDefault
        )
    }

    @Test
    suspend fun `new default address should replace previous default`() {

        val user =
            createUser()

        userRepository.save(
            user
        )

        val first =
            createAddress(
                userId = user.id,
                label = "Casa",
                street = "Calle 23",
                isDefault = true
            )

        addressRepository.save(
            first
        )

        val second =
            createAddress(
                userId = user.id,
                label = "Trabajo",
                street = "Calle L",
                isDefault = true
            )

        addressRepository.save(
            second
        )

        val addresses =
            addressRepository.findByUser(
                user.id
            )

        assertEquals(
            2,
            addresses.size
        )

        val oldDefault =
            addresses.first {
                it.id == first.id
            }

        val newDefault =
            addresses.first {
                it.id == second.id
            }

        assertFalse(
            oldDefault.isDefault
        )

        assertTrue(
            newDefault.isDefault
        )
    }

    private fun createUser(): User {

        val now =
            Instant.now()

        return User(
            id = UUID.randomUUID(),
            email = "address@test.com",
            phone = "+5355550100",
            passwordHash =
                "\$2a\$12\$fakeHash",
            firstName = "Address",
            lastName = "Tester",
            roles =
                setOf(
                    Role.CUSTOMER
                ),
            isActive = true,
            createdAt = now,
            updatedAt = now
        )
    }

    private fun createAddress(
        userId: UUID,
        label: String = "Casa",
        street: String = "Calle 1",
        isDefault: Boolean = false
    ): Address {

        val now =
            Instant.now()

        return Address(
            id =
                UUID.randomUUID(),

            userId =
                userId,

            label =
                label,

            street =
                street,

            number =
                "100",

            neighborhood =
                "Centro",

            city =
                "La Habana",

            province =
                "La Habana",

            reference =
                "Frente al parque",

            latitude =
                null,

            longitude =
                null,

            isDefault =
                isDefault,

            createdAt =
                now,

            updatedAt =
                now
        )
    }
}