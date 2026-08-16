package com.blackneko.infrastructure.database.repository

import com.blackneko.application.TransactionRunner
import com.blackneko.domain.user.Role
import com.blackneko.domain.user.User
import com.blackneko.domain.user.UserRepository
import com.blackneko.infrastructure.database.mapper.toUser
import com.blackneko.infrastructure.database.table.UserRolesTable
import com.blackneko.infrastructure.database.table.UsersTable
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import java.util.UUID

class UserRepositoryImpl(
    transactionRunner: TransactionRunner
) : BaseRepository(transactionRunner),
    UserRepository {

    override suspend fun findById(
        id: UUID
    ): User? =
        transactionRunner.transaction {

            val row =
                UsersTable
                    .selectAll()
                    .where {
                        UsersTable.id eq id
                    }
                    .singleOrNull()

            row ?: return@transaction null

            val roles =
                UserRolesTable
                    .selectAll()
                    .where {

                        UserRolesTable.user eq id

                    }
                    .map {

                        it[UserRolesTable.role]
                    }
                    .map(Role::valueOf)
                    .toSet()

            row.toUser(roles)
        }

    override suspend fun findByEmail(
        email: String
    ): User? =
        transactionRunner.transaction {

            val row =
                UsersTable
                    .selectAll()
                    .where {
                        UsersTable.email eq email
                    }
                    .singleOrNull()

            row ?: return@transaction null

            val roles =
                loadRoles(
                    row[UsersTable.id].value
                )

            row.toUser(roles)
        }

    override suspend fun findByPhone(phone: String): User? =
        transactionRunner.transaction {

            val row =
                UsersTable
                    .selectAll()
                    .where {
                        UsersTable.phone eq phone
                    }
                    .singleOrNull()

            row ?: return@transaction null

            val roles =
                loadRoles(
                    row[UsersTable.id].value
                )

            row.toUser(roles)
        }


    override suspend fun save(
        user: User
    ): User =
        transactionRunner.transaction {

            UsersTable.insert {

                it[UsersTable.id] = user.id

                it[UsersTable.email] = user.email

                it[phone] = user.phone

                it[firstName] = user.firstName

                it[lastName] = user.lastName

                it[passwordHash] =
                    user.passwordHash

                it[isActive] =
                    user.isActive

                it[createdAt] =
                    user.createdAt

                it[updatedAt] =
                    user.updatedAt
            }

            user.roles.forEach {

                UserRolesTable.insert { role ->

                    role[UserRolesTable.user] = user.id

                    role[UserRolesTable.role] =
                        it.name
                }
            }

            user
        }

    override suspend fun update(
        user: User
    ): User =
        transactionRunner.transaction {

            UsersTable.update({

                UsersTable.id eq user.id

            }) {

                it[email] = user.email

                it[phone] = user.phone

                it[firstName] =
                    user.firstName

                it[lastName] =
                    user.lastName

                it[passwordHash] =
                    user.passwordHash

                it[isActive] =
                    user.isActive

                it[updatedAt] =
                    user.updatedAt
            }

            UserRolesTable.deleteWhere {

                UserRolesTable.user eq user.id

            }

            user.roles.forEach {

                UserRolesTable.insert { row ->

                    row[UserRolesTable.user] = user.id

                    row[role] = it.name
                }
            }

            user
        }

    override suspend fun delete(
        id: UUID
    ) =
        transactionRunner.transaction {

            UsersTable.deleteWhere {

                UsersTable.id eq id

            }
        }


    override suspend fun findAll(): List<User> =
        transactionRunner.transaction {

            UsersTable
                .selectAll()
                .map {

                    it.toUser(
                        loadRoles(
                            it[UsersTable.id].value
                        )
                    )
                }
        }

    override suspend fun existsByEmail(
        email: String
    ): Boolean =
        transactionRunner.transaction {

            !UsersTable
                .selectAll()
                .where {

                    UsersTable.email eq email

                }
                .empty()
        }

    override suspend fun existsByPhone(
        phone: String
    ): Boolean =
        transactionRunner.transaction {

            !UsersTable
                .selectAll()
                .where {

                    UsersTable.phone eq phone

                }
                .empty()
        }

}

private fun loadRoles(
    id: UUID
): Set<Role> {

    return UserRolesTable

        .selectAll()

        .where {

            UserRolesTable.user eq id

        }

        .map {

            Role.valueOf(
                it[UserRolesTable.role]
            )

        }

        .toSet()
}