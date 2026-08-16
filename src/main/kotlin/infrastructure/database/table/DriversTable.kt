package com.blackneko.infrastructure.database.table

import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.javatime.timestamp

object DriversTable : UUIDTable("drivers") {

    val user =
        reference(
            "user_id",
            UsersTable
        ).uniqueIndex()

    val status =
        varchar(
            "status",
            30
        )

    val vehicleType =
        varchar(
            "vehicle_type",
            30
        ).nullable()

    val vehicleDescription =
        varchar(
            "vehicle_description",
            255
        ).nullable()

    val createdAt =
        timestamp("created_at")

    val updatedAt =
        timestamp("updated_at")
}