package com.blackneko.infrastructure.database.mapper

import com.blackneko.domain.auth.RefreshToken
import com.blackneko.infrastructure.database.table.RefreshTokensTable
import org.jetbrains.exposed.v1.core.ResultRow

fun ResultRow.toRefreshToken():
        RefreshToken {

    return RefreshToken(
        id =
            this[
                RefreshTokensTable.id
            ].value,

        userId =
            this[
                RefreshTokensTable.user
            ].value,

        tokenHash =
            this[
                RefreshTokensTable.tokenHash
            ],

        expiresAt =
            this[
                RefreshTokensTable.expiresAt
            ],

        revokedAt =
            this[
                RefreshTokensTable.revokedAt
            ],

        createdAt =
            this[
                RefreshTokensTable.createdAt
            ]
    )
}