package com.blackneko.infrastructure.database.mapper

import com.blackneko.domain.order.Order
import com.blackneko.domain.order.OrderStatus
import com.blackneko.domain.shared.Money
import com.blackneko.infrastructure.database.table.OrdersTable
import org.jetbrains.exposed.v1.core.ResultRow

fun ResultRow.toOrder(): Order =
    Order(
        id =
            this[OrdersTable.id].value,

        customerId =
            this[OrdersTable.customer].value,

        restaurantId =
            this[OrdersTable.restaurant].value,

        driverId =
            this[OrdersTable.driver]?.value,

        deliveryAddressId =
            this[OrdersTable.deliveryAddress].value,

        status =
            OrderStatus.valueOf(
                this[OrdersTable.status]
            ),

        subtotal =
            Money(
                this[OrdersTable.subtotal]
            ),

        deliveryFee =
            Money(
                this[OrdersTable.deliveryFee]
            ),

        total =
            Money(
                this[OrdersTable.total]
            ),

        notes =
            this[OrdersTable.notes],

        createdAt =
            this[OrdersTable.createdAt],

        confirmedAt =
            this[OrdersTable.confirmedAt],

        preparingAt =
            this[OrdersTable.preparingAt],

        readyAt =
            this[OrdersTable.readyAt],

        pickedUpAt =
            this[OrdersTable.pickedUpAt],

        deliveredAt =
            this[OrdersTable.deliveredAt],

        cancelledAt =
            this[OrdersTable.cancelledAt],

        updatedAt =
            this[OrdersTable.updatedAt]
    )