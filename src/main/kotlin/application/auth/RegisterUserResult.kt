package com.blackneko.application.auth

import java.util.UUID

data class RegisterUserResult(
    val userId: UUID,
    val accessToken: String
)