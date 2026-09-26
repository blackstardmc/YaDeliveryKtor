package com.blackneko.application.auth

import com.blackneko.application.exception.NotFoundException
import com.blackneko.domain.user.User
import com.blackneko.domain.user.UserRepository
import java.util.UUID

class GetCurrentUserUseCase(
    private val userRepository: UserRepository
) {

    suspend operator fun invoke(
        userId: UUID
    ): User {

        return userRepository.findById(
            userId
        ) ?: throw NotFoundException(
            "User not found"
        )
    }
}