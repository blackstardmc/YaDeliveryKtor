package com.blackneko

import com.blackneko.domain.shared.Money
import org.junit.jupiter.api.Assertions.assertEquals
import kotlin.test.Test
import kotlin.test.assertFailsWith


class MoneyTest {
    @org.junit.jupiter.api.Test
    fun `money arithmetic rejects overflow`() {
        kotlin.test.assertFailsWith<ArithmeticException> { Money(Long.MAX_VALUE) + Money(1) }
        kotlin.test.assertFailsWith<ArithmeticException> { Money(Long.MAX_VALUE) * 2 }
    }

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
