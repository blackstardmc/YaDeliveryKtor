package com.blackneko.infrastructure.database.mapper

import com.blackneko.domain.order.OrderStatus
import com.blackneko.domain.order.OrderStatusHistory
import com.blackneko.infrastructure.database.table.OrderStatusHistoryTable
import org.jetbrains.exposed.v1.core.ResultRow

fun ResultRow.toOrderStatusHistory(): OrderStatusHistory =
    OrderStatusHistory(
        id =
            this[OrderStatusHistoryTable.id].value,

        orderId =
            this[OrderStatusHistoryTable.order].value,

        fromStatus =
            this[OrderStatusHistoryTable.fromStatus]
                ?.let {
                    OrderStatus.valueOf(it)
                },

        toStatus =
            OrderStatus.valueOf(
                this[OrderStatusHistoryTable.toStatus]
            ),

        changedBy =
            this[OrderStatusHistoryTable.changedBy]
                ?.value,

        createdAt =
            this[OrderStatusHistoryTable.createdAt]
    )