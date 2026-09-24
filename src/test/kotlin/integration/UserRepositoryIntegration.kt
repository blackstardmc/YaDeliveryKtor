package com.blackneko.integration

import com.blackneko.domain.user.Role
import com.blackneko.domain.user.User
import com.blackneko.infrastructure.database.repository.UserRepositoryImpl
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class UserRepositoryIntegrationTest :
    DatabaseIntegrationTest() {

    private val repository by lazy {
        UserRepositoryImpl(
            transactionRunner
        )
    }

    @Test
    suspend fun `should save and retrieve user`() {

        val user =
            createUser(
                phone = "+5355550001",
                email = "user1@test.com"
            )

        repository.save(
            user
        )

        val result =
            repository.findById(
                user.id
            )

        assertNotNull(
            result
        )

        assertEquals(
            user.id,
            result.id
        )

        assertEquals(
            user.email,
            result.email
        )

        assertEquals(
            user.phone,
            result.phone
        )

        assertEquals(
            user.firstName,
            result.firstName
        )

        assertEquals(
            user.lastName,
            result.lastName
        )

        assertEquals(
            user.roles,
            result.roles
        )
    }

    @Test
    suspend fun `should find user by phone`() {

        val user =
            createUser(
                phone = "+5355550002",
                email = "user2@test.com"
            )

        repository.save(
            user
        )

        val result =
            repository.findByPhone(
                user.phone
            )

        assertNotNull(
            result
        )

        assertEquals(
            user.id,
            result.id
        )
    }

    @Test
    suspend fun `should find user by email`() {

        val user =
            createUser(
                phone = "+5355550003",
                email = "user3@test.com"
            )

        repository.save(
            user
        )

        val result =
            repository.findByEmail(
                "user3@test.com"
            )

        assertNotNull(
            result
        )

        assertEquals(
            user.id,
            result.id
        )
    }

    @Test
    suspend fun `should persist multiple roles`() {

        val user =
            createUser(
                phone = "+5355550004",
                email = "multi@test.com",
                roles =
                    setOf(
                        Role.CUSTOMER,
                        Role.DRIVER
                    )
            )

        repository.save(
            user
        )

        val result =
            repository.findById(
                user.id
            )

        assertNotNull(
            result
        )

        assertEquals(
            setOf(
                Role.CUSTOMER,
                Role.DRIVER
            ),
            result.roles
        )
    }

    @Test
    suspend fun `should delete user`() {

        val user =
            createUser(
                phone = "+5355550005",
                email = "delete@test.com"
            )

        repository.save(
            user
        )

        repository.delete(
            user.id
        )

        val result =
            repository.findById(
                user.id
            )

        assertNull(
            result
        )
    }

    private fun createUser(
        phone: String,
        email: String?,
        roles: Set<Role> =
            setOf(
                Role.CUSTOMER
            )
    ): User {

        val now =
            Instant.now()

        return User(
            id =
                UUID.randomUUID(),

            email =
                email,

            phone =
                phone,

            passwordHash =
                "\$2a\$12\$fakeHashForIntegrationTest",

            firstName =
                "Test",

            lastName =
                "User",

            roles =
                roles,

            isActive =
                true,

            createdAt =
                now,

            updatedAt =
                now
        )
    }
}