package com.blackneko.domain.order

import com.blackneko.domain.shared.Money
import java.time.Instant
import java.util.UUID

object OrderFactory {

    fun create(
        id: UUID,
        customerId: UUID,
        restaurantId: UUID,
        deliveryAddressId: UUID,
        subtotal: Money,
        deliveryFee: Money,
        notes: String?
    ): Order {

       val now = Instant.now()

        return Order(
            id = id,
            customerId = customerId,
            restaurantId = restaurantId,
            driverId = null,
            deliveryAddressId = deliveryAddressId,
            status = OrderStatus.RECEIVED,
            subtotal = subtotal,
            deliveryFee = deliveryFee,
            total = subtotal + deliveryFee,
            notes = notes,
            createdAt = now,
            confirmedAt = null,
            preparingAt = null,
            readyAt = null,
            pickedUpAt = null,
            deliveredAt = null,
            cancelledAt = null,
            updatedAt = now
        )
    }
}