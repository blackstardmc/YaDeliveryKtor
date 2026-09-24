package com.blackneko.application.auth

import com.blackneko.domain.user.Role
import java.util.UUID

data class LoginResult(
    val userId: UUID,
    val roles: Set<Role>,
    val accessToken: String
)