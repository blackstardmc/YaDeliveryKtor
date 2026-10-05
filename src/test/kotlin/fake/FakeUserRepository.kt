package com.blackneko.fake

import com.blackneko.domain.user.User
import com.blackneko.domain.user.UserRepository
import java.util.UUID

class FakeUserRepository :
    UserRepository {

    private val users =
        mutableMapOf<
                UUID,
                User
                >()

    override suspend fun save(
        user: User
    ): User {

        users[
            user.id
        ] = user
        return user
    }

    override suspend fun update(
        user: User
    ): User {

        users[
            user.id
        ] = user
        return user
    }

    override suspend fun findById(
        id: UUID
    ): User? =
        users[
            id
        ]

    override suspend fun findByEmail(
        email: String
    ): User? =
        users.values
            .firstOrNull {
                it.email == email
            }

    override suspend fun findByPhone(
        phone: String
    ): User? =
        users.values
            .firstOrNull {
                it.phone == phone
            }

    override suspend fun existsByEmail(
        email: String
    ): Boolean =
        users.values.any {
            it.email == email
        }

    override suspend fun existsByPhone(
        phone: String
    ): Boolean =
        users.values.any {
            it.phone == phone
        }

    override suspend fun delete(
        id: UUID
    ): Int {

        return if (users.remove(id) == null) 0 else 1
    }

    override suspend fun findAll(page: com.blackneko.domain.shared.PageRequest):
            List<User> =
        users.values
            .toList()
}
