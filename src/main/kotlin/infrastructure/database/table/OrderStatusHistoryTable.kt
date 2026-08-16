package com.blackneko.infrastructure.database.table

import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.javatime.timestamp

object OrderStatusHistoryTable :
    UUIDTable("order_status_history") {

    val order =
        reference(
            "order_id",
            OrdersTable
        )

    val fromStatus =
        varchar(
            "from_status",
            30
        ).nullable()

    val toStatus =
        varchar(
            "to_status",
            30
        )

    val changedBy =
        optReference(
            "changed_by",
            UsersTable
        )

    val createdAt =
        timestamp("created_at")
}