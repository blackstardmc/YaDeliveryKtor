package com.blackneko.infrastructure.database.mapper

import com.blackneko.domain.order.OrderItem
import com.blackneko.domain.shared.Money
import com.blackneko.infrastructure.database.table.OrderItemsTable
import org.jetbrains.exposed.v1.core.ResultRow

fun ResultRow.toOrderItem(): OrderItem =
    OrderItem(
        id =
            this[OrderItemsTable.id].value,

        orderId =
            this[OrderItemsTable.order].value,

        productId =
            this[OrderItemsTable.product].value,

        productName =
            this[OrderItemsTable.productName],

        unitPrice =
            Money(
                this[OrderItemsTable.unitPrice]
            ),

        quantity =
            this[OrderItemsTable.quantity],

        subtotal =
            Money(
                this[OrderItemsTable.subtotal]
            )
    )