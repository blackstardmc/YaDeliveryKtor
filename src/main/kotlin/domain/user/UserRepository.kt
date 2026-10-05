package com.blackneko.domain.user

import java.util.UUID
import com.blackneko.domain.shared.PageRequest


interface UserRepository {

    suspend fun findById(
        id: UUID
    ): User?

    suspend fun findByEmail(
        email: String
    ): User?

    suspend fun findByPhone(
        phone: String
    ): User?

    suspend fun save(
        user: User
    ): User

    suspend fun update(
        user: User
    ): User


    suspend fun delete(id: UUID): Int

    suspend fun existsByEmail(
        email: String
    ): Boolean

    suspend fun existsByPhone(
        phone: String
    ): Boolean

    suspend fun findAll(page: PageRequest = PageRequest()): List<User>
}
