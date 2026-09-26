package com.blackneko.infrastructure.database.table

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.javatime.timestamp

object RefreshTokensTable :
    UUIDTable("refresh_tokens") {

    val user =
        reference(
            name = "user_id",
            foreign = UsersTable,
            onDelete = ReferenceOption.CASCADE
        )

    val tokenHash =
        varchar(
            "token_hash",
            64
        )
            .uniqueIndex()

    val expiresAt =
        timestamp(
            "expires_at"
        )

    val revokedAt =
        timestamp(
            "revoked_at"
        )
            .nullable()

    val createdAt =
        timestamp(
            "created_at"
        )
}