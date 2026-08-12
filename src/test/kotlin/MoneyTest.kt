package com.blackneko

import com.blackneko.domain.shared.Money
import org.junit.jupiter.api.Assertions.assertEquals
import kotlin.test.Test
import kotlin.test.assertFailsWith


class MoneyTest {

    @Test
    fun `should add money`() {

        val first = Money(1000)
        val second = Money(500)

        val result = first + second

        assertEquals(
            Money(1500),
            result
        )
    }

    @Test
    fun `should reject negative money`() {

        assertFailsWith<IllegalArgumentException> {

            Money(-1)
        }
    }
}