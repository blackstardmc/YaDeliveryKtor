package com.blackneko.domain.user

import java.util.UUID

interface UserRepository {

    suspend fun findById(id: UUID): User?

    suspend fun findByPhone(phone: String): User?

    suspend fun findByEmail(email: String): User?

    suspend fun save(user: User): User

    suspend fun update(user: User): User
}