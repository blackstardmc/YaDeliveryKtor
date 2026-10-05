package com.blackneko.auth

import com.blackneko.application.TransactionRunner
import com.blackneko.application.auth.*
import com.blackneko.application.exception.AuthenticationException
import com.blackneko.application.security.*
import com.blackneko.domain.auth.*
import com.blackneko.domain.user.*
import com.blackneko.fake.*
import com.blackneko.infrastructure.security.SecureRefreshTokenGenerator
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.util.UUID
import kotlin.test.*

class LoginUseCaseTest {
    @Test
    fun `actual login checks credentials and persists only hashed refresh tokens`() = runBlocking {
        val users = FakeUserRepository()
        val passwords = BCryptPasswordHasher()
        val stored = mutableListOf<RefreshToken>()
        val tokens = object : RefreshTokenRepository {
            override suspend fun save(token: RefreshToken) { stored += token }
            override suspend fun findByHash(tokenHash: String) = stored.firstOrNull { it.tokenHash == tokenHash }
            override suspend fun revokeIfActive(id: UUID, revokedAt: Instant) = false
            override suspend fun revokeAllByUser(userId: UUID, revokedAt: Instant) = Unit
            override suspend fun deleteExpired(now: Instant) = 0
        }
        val runner = object : TransactionRunner {
            override suspend fun <T> transaction(block: suspend () -> T) = block()
        }
        val access = FakeTokenService()
        val generator = SecureRefreshTokenGenerator()
        val session = CreateAuthSessionUseCase(access, generator, tokens, runner, Clock.systemUTC(), TokenSettings(30))
        val login = LoginUseCase(users, passwords, access, session)
        val now = Instant.now()
        val user = User(UUID.randomUUID(), "login@test.com", "+5355558888", passwords.hash("Password123!"),
            "Login", "Test", setOf(Role.CUSTOMER), true, now, now)
        users.save(user)
        val wrong = assertFailsWith<AuthenticationException> { login(LoginCommand(user.email!!, "wrong")) }
        val absent = assertFailsWith<AuthenticationException> { login(LoginCommand("absent@test.com", "wrong")) }
        assertEquals(wrong.message, absent.message)
        assertTrue(stored.isEmpty())
        val result = login(LoginCommand(" LOGIN@TEST.COM ", "Password123!"))
        assertEquals(user.id, result.userId)
        assertEquals(user.roles, result.roles)
        assertEquals(generator.hash(result.refreshToken), stored.single().tokenHash)
        assertNotEquals(result.refreshToken, stored.single().tokenHash)
        users.update(user.copy(isActive = false))
        assertFailsWith<AuthenticationException> { login(LoginCommand(user.phone, "Password123!")) }
        assertEquals(1, stored.size)
    }
}
