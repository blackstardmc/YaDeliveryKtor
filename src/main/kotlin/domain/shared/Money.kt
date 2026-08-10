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

    operator fun plus(other: Money): Money =
        Money(cents + other.cents)

    operator fun minus(other: Money): Money {
        require(cents >= other.cents) {
            "Money result cannot be negative"
        }

        return Money(cents - other.cents)
    }
}