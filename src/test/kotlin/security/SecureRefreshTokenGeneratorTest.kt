package com.blackneko.security

import com.blackneko.infrastructure.security.SecureRefreshTokenGenerator
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class SecureRefreshTokenGeneratorTest {

    private val generator =
        SecureRefreshTokenGenerator()

    @Test
    fun `should generate random refresh tokens`() {

        val first =
            generator.generate()

        val second =
            generator.generate()

        assertNotEquals(
            first,
            second
        )

        assertTrue(
            first.isNotBlank()
        )

        assertTrue(
            second.isNotBlank()
        )
    }

    @Test
    fun `same token should generate same hash`() {

        val token =
            generator.generate()

        val firstHash =
            generator.hash(
                token
            )

        val secondHash =
            generator.hash(
                token
            )

        assertEquals(
            firstHash,
            secondHash
        )
    }

    @Test
    fun `different tokens should generate different hashes`() {

        val first =
            generator.generate()

        val second =
            generator.generate()

        assertNotEquals(
            generator.hash(first),
            generator.hash(second)
        )
    }

    @Test
    fun `sha256 hash should contain 64 hex characters`() {

        val token =
            generator.generate()

        val hash =
            generator.hash(
                token
            )

        assertEquals(
            64,
            hash.length
        )

        assertTrue(
            hash.matches(
                Regex(
                    "^[0-9a-f]{64}$"
                )
            )
        )
    }
}