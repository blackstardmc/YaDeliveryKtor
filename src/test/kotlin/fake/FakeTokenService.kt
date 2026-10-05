package com.blackneko.fake

import com.blackneko.application.security.TokenService
import com.blackneko.domain.user.User

class FakeTokenService :
    TokenService {

    override fun generateAccessToken(
        user: User
    ): String =
        "access-token-${user.id}"
}