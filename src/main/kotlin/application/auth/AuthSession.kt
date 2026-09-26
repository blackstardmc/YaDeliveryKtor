package com.blackneko.application.auth

import com.blackneko.domain.user.Role
import java.util.UUID

data class AuthSession(
    val userId: UUID,
    val roles: Set<Role>,
    val accessToken: String,
    val refreshToken: String
)