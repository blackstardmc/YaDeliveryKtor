package com.blackneko.infrastructure.database.repository

import com.blackneko.application.TransactionRunner
import com.blackneko.domain.auth.RefreshToken
import com.blackneko.domain.auth.RefreshTokenRepository
import com.blackneko.infrastructure.database.mapper.toRefreshToken
import com.blackneko.infrastructure.database.table.RefreshTokensTable
import com.blackneko.infrastructure.database.table.UsersTable
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.jdbc.*
import java.time.Instant
import java.util.UUID

class RefreshTokenRepositoryImpl(
    transactionRunner: TransactionRunner
) : BaseRepository(
    transactionRunner
), RefreshTokenRepository {

    override suspend fun save(
        token: RefreshToken
    ) {
        transactionRunner.transaction {

            RefreshTokensTable.insert {

                it[id] =
                    EntityID(
                        token.id,
                        RefreshTokensTable
                    )

                it[user] =
                    EntityID(
                        token.userId,
                        UsersTable
                    )

                it[tokenHash] =
                    token.tokenHash

                it[expiresAt] =
                    token.expiresAt

                it[revokedAt] =
                    token.revokedAt

                it[createdAt] =
                    token.createdAt
            }
        }
    }

    override suspend fun findByHash(
        tokenHash: String
    ): RefreshToken? =
        transactionRunner.transaction {

            RefreshTokensTable
                .selectAll()
                .where {
                    RefreshTokensTable.tokenHash eq
                            tokenHash
                }
                .limit(1)
                .map {
                    it.toRefreshToken()
                }
                .singleOrNull()
        }

    override suspend fun revoke(
        id: UUID,
        revokedAt: Instant
    ) {
        transactionRunner.transaction {

            RefreshTokensTable.update(
                where = {
                    RefreshTokensTable.id eq id
                }
            ) {
                it[
                    RefreshTokensTable.revokedAt
                ] = revokedAt
            }
        }
    }

    override suspend fun revokeAllByUser(
        userId: UUID,
        revokedAt: Instant
    ) {
        transactionRunner.transaction {

            RefreshTokensTable.update(
                where = {
                    (RefreshTokensTable.user eq
                            EntityID(
                                userId,
                                UsersTable
                            )) and

                            RefreshTokensTable
                                .revokedAt
                                .isNull()
                }
            ) {
                it[
                    RefreshTokensTable.revokedAt
                ] = revokedAt
            }
        }
    }

    override suspend fun deleteExpired(
        now: Instant
    ): Int =
        transactionRunner.transaction {

            RefreshTokensTable.deleteWhere {
                expiresAt lessEq now
            }
        }
}