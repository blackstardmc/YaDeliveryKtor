package com.blackneko.infrastructure.database.table

import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.javatime.timestamp

object UsersTable : UUIDTable("users") {

    val email = varchar("email", 255).nullable()

    val phone = varchar("phone", 20).uniqueIndex()

    val passwordHash = varchar("password_hash", 255)

    val firstName = varchar("first_name", 100)

    val lastName = varchar("last_name", 100)

    val isActive = bool("is_active")

    val createdAt = timestamp("created_at")

    val updatedAt = timestamp("updated_at")
}