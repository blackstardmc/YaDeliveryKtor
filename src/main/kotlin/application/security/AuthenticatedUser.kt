package com.blackneko.application.security


import com.blackneko.domain.user.Role
import java.util.UUID

data class AuthenticatedUser(
    val userId: UUID,
    val roles: Set<Role>
)