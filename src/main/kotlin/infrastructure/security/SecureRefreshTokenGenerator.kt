package com.blackneko.infrastructure.security

import com.blackneko.application.security.RefreshTokenGenerator
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

class SecureRefreshTokenGenerator :
    RefreshTokenGenerator {

    private val secureRandom =
        SecureRandom()

    override fun generate(): String {

        val bytes =
            ByteArray(32)

        secureRandom.nextBytes(
            bytes
        )

        return Base64
            .getUrlEncoder()
            .withoutPadding()
            .encodeToString(
                bytes
            )
    }

    override fun hash(
        token: String
    ): String {

        val digest =
            MessageDigest.getInstance(
                "SHA-256"
            )
                .digest(
                    token.toByteArray(
                        Charsets.UTF_8
                    )
                )

        return digest.joinToString(
            separator = ""
        ) {
            "%02x".format(it)
        }
    }
}