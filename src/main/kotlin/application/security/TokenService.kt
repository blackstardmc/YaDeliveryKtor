package com.blackneko.application.security

import com.blackneko.domain.user.User

interface TokenService {

    fun generateAccessToken(
        user: User
    ): String
}