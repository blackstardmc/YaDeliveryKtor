package com.blackneko.domain.shared


@JvmInline
value class Money(
    val cents: Long
) {

    init {
        require(cents >= 0) {
            "Money cannot be negative"
        }
    }

    operator fun plus(other: Money): Money {
        return Money(
            cents + other.cents
        )
    }

    operator fun minus(other: Money): Money {
        require(cents >= other.cents) {
            "Money result cannot be negative"
        }

        return Money(
            cents - other.cents
        )
    }

    operator fun times(quantity: Int): Money {
        require(quantity >= 0) {
            "Quantity cannot be negative"
        }

        return Money(
            cents * quantity
        )
    }

    fun isZero(): Boolean {
        return cents == 0L
    }

    companion object {

        val ZERO = Money(0)

        fun fromCents(cents: Long): Money {
            return Money(cents)
        }
    }
}