package com.blackneko.security


import com.blackneko.application.security.BCryptPasswordHasher
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class BCryptPasswordHasherTest {

    private val passwordHasher =
        BCryptPasswordHasher()

    @Test
    fun `should hash password`() {

        val password =
            "Password123!"

        val hash =
            passwordHasher.hash(
                password
            )

        assertNotEquals(
            password,
            hash
        )

        assertTrue(
            hash.startsWith(
                "\$2"
            )
        )
    }

    @Test
    fun `should verify correct password`() {

        val password =
            "Password123!"

        val hash =
            passwordHasher.hash(
                password
            )

        val result =
            passwordHasher.verify(
                password =
                    password,

                hash =
                    hash
            )

        assertTrue(
            result
        )
    }

    @Test
    fun `should reject incorrect password`() {

        val hash =
            passwordHasher.hash(
                "Password123!"
            )

        val result =
            passwordHasher.verify(
                password =
                    "WrongPassword",

                hash =
                    hash
            )

        assertFalse(
            result
        )
    }

    @Test
    fun `should generate different hashes for same password`() {

        val password =
            "Password123!"

        val first =
            passwordHasher.hash(
                password
            )

        val second =
            passwordHasher.hash(
                password
            )

        assertNotEquals(
            first,
            second
        )

        assertTrue(
            passwordHasher.verify(
                password,
                first
            )
        )

        assertTrue(
            passwordHasher.verify(
                password,
                second
            )
        )
    }
}