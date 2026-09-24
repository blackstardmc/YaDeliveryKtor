package com.blackneko.application.security

import org.mindrot.jbcrypt.BCrypt

class BCryptPasswordHasher : PasswordHasher {

    override fun hash(
        password: String
    ): String {

        return BCrypt.hashpw(
            password,
            BCrypt.gensalt(WORK_FACTOR)
        )
    }

    override fun verify(
        password: String,
        hash: String
    ): Boolean {

        return try {

            BCrypt.checkpw(
                password,
                hash
            )

        } catch (_: IllegalArgumentException) {

            false
        }
    }

    companion object {

        private const val WORK_FACTOR = 12
    }
}