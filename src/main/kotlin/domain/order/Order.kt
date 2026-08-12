package com.blackneko.domain.order

import com.blackneko.domain.shared.InvalidOrderException
import com.blackneko.domain.shared.InvalidOrderStatusTransitionException
import com.blackneko.domain.shared.Money
import java.util.UUID
import kotlin.time.Clock
import kotlin.time.Instant

data class Order(
    val id: UUID,
    val customerId: UUID,
    val restaurantId: UUID,
    val driverId: UUID?,
    val deliveryAddressId: UUID,
    val status: OrderStatus,
    val subtotal: Money,
    val deliveryFee: Money,
    val total: Money,
    val notes: String?,
    val createdAt: Instant,
    val confirmedAt: Instant?,
    val preparingAt: Instant?,
    val readyAt: Instant?,
    val pickedUpAt: Instant?,
    val deliveredAt: Instant?,
    val cancelledAt: Instant?,
    val updatedAt: Instant
) {
    init {

        require(subtotal.cents >= 0) {
            "Subtotal cannot be negative"
        }

        require(deliveryFee.cents >= 0) {
            "Delivery fee cannot be negative"
        }

        require(total == subtotal + deliveryFee) {
            "Order total is invalid"
        }
    }

    fun assignDriver(
        driverId: UUID
    ): Order {

        if (status != OrderStatus.READY) {
            throw InvalidOrderException(
                "Driver can only be assigned when order is READY"
            )
        }

        return copy(
            driverId = driverId,
            updatedAt = Clock.System.now(),
        )
    }

    fun transitionTo(
        newStatus: OrderStatus
    ): Order {

        if (
            !OrderStatusTransitions.canTransition(
                status,
                newStatus
            )
        ) {
            throw InvalidOrderStatusTransitionException(
                "Cannot transition order from $status to $newStatus"
            )
        }

        val now = Clock.System.now()

        return when (newStatus) {

            OrderStatus.CONFIRMED ->
                copy(
                    status = newStatus,
                    confirmedAt = now,
                    updatedAt = now
                )

            OrderStatus.PREPARING ->
                copy(
                    status = newStatus,
                    preparingAt = now,
                    updatedAt = now
                )

            OrderStatus.READY ->
                copy(
                    status = newStatus,
                    readyAt = now,
                    updatedAt = now
                )

            OrderStatus.IN_DELIVERY ->
                copy(
                    status = newStatus,
                    pickedUpAt = now,
                    updatedAt = now
                )

            OrderStatus.DELIVERED ->
                copy(
                    status = newStatus,
                    deliveredAt = now,
                    updatedAt = now
                )

            OrderStatus.CANCELLED ->
                copy(
                    status = newStatus,
                    cancelledAt = now,
                    updatedAt = now
                )

            OrderStatus.RECEIVED ->
                throw InvalidOrderStatusTransitionException(
                    "Cannot transition to RECEIVED"
                )
        }
    }

    fun canBeCancelled(): Boolean {

        return status in setOf(
            OrderStatus.RECEIVED,
            OrderStatus.CONFIRMED,
            OrderStatus.PREPARING
        )
    }

    fun cancel(): Order {

        if (!canBeCancelled()) {
            throw InvalidOrderException(
                "Order cannot be cancelled in status $status"
            )
        }

        return transitionTo(
            OrderStatus.CANCELLED
        )
    }

    fun isCompleted(): Boolean {
        return status == OrderStatus.DELIVERED
    }

    fun isCancelled(): Boolean {
        return status == OrderStatus.CANCELLED
    }
}

