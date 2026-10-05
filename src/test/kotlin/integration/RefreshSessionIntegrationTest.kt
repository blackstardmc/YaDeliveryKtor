package com.blackneko.integration

import com.blackneko.application.auth.*
import com.blackneko.application.exception.AuthenticationException
import com.blackneko.application.security.*
import com.blackneko.domain.auth.*
import com.blackneko.domain.user.*
import com.blackneko.infrastructure.database.repository.*
import com.blackneko.infrastructure.security.SecureRefreshTokenGenerator
import kotlinx.coroutines.*
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.*

class RefreshSessionIntegrationTest : DatabaseIntegrationTest() {
    private val users by lazy { UserRepositoryImpl(transactionRunner) }
    private val tokens by lazy { RefreshTokenRepositoryImpl(transactionRunner) }
    private val generator = SecureRefreshTokenGenerator()
    private val clock = Clock.systemUTC()
    private val tokenService = object : TokenService {
        override fun generateAccessToken(user: User) = "access-${user.id}"
    }

    private fun useCase(repository: RefreshTokenRepository = tokens) = RefreshSessionUseCase(
        repository, users, generator, tokenService, transactionRunner, clock, TokenSettings(30))

    private suspend fun setup(expiry: Instant = Instant.now().plusSeconds(3600)): Pair<String, RefreshToken> {
        val now = Instant.now()
        val user = User(UUID.randomUUID(), "refresh@test.com", "+5355550100", "hash", "Refresh", "Test",
            setOf(Role.CUSTOMER), true, now, now)
        users.save(user)
        val raw = generator.generate()
        val token = RefreshToken(UUID.randomUUID(), user.id, generator.hash(raw), expiry, null, now)
        tokens.save(token)
        return raw to token
    }

    @Test
    fun `simultaneous full refresh requests yield one session and one authentication failure`(): Unit = runBlocking {
        val (raw, token) = setup()
        val reads = AtomicInteger()
        val bothRead = CompletableDeferred<Unit>()
        val synchronizedRepository = object : RefreshTokenRepository by tokens {
            override suspend fun findByHash(tokenHash: String): RefreshToken? {
                val found = tokens.findByHash(tokenHash)
                if (reads.incrementAndGet() == 2) bothRead.complete(Unit)
                bothRead.await()
                return found
            }
        }
        val refresh = useCase(synchronizedRepository)
        val results = withTimeout(20_000) {
            coroutineScope { List(2) { async(Dispatchers.IO) { runCatching { refresh(raw) } } }.awaitAll() }
        }
        assertEquals(1, results.count { it.isSuccess })
        assertEquals(1, results.count { it.exceptionOrNull() is AuthenticationException })
        assertNotNull(tokens.findByHash(token.tokenHash)?.revokedAt)
        val session = results.single { it.isSuccess }.getOrThrow()
        assertNotEquals(raw, session.refreshToken)
        assertTrue(assertNotNull(tokens.findByHash(generator.hash(session.refreshToken))).isValid(clock.instant()))
        assertFailsWith<AuthenticationException> { useCase()(raw) }
    }

    @Test
    fun `failed replacement rolls back revocation and insertion`(): Unit = runBlocking {
        val (raw, original) = setup()
        var replacementHash: String? = null
        val failing = object : RefreshTokenRepository by tokens {
            override suspend fun save(token: RefreshToken) {
                replacementHash = token.tokenHash
                tokens.save(token)
                throw IllegalStateException("Simulated insertion failure")
            }
        }
        assertFailsWith<IllegalStateException> { useCase(failing)(raw) }
        assertNull(tokens.findByHash(original.tokenHash)?.revokedAt)
        assertNull(tokens.findByHash(assertNotNull(replacementHash)))
        assertNotEquals(raw, useCase()(raw).refreshToken)
    }

    @Test
    fun `expired inactive unknown and blank tokens rejected`(): Unit = runBlocking {
        val (raw, token) = setup(Instant.now().minusSeconds(1))
        assertFailsWith<AuthenticationException> { useCase()(raw) }
        assertFailsWith<AuthenticationException> { useCase()("unknown") }
        assertFailsWith<AuthenticationException> { useCase()(" ") }
        assertEquals(1, tokens.deleteExpired(clock.instant()))
        assertNull(tokens.findByHash(token.tokenHash))
    }

    @Test
    fun `revoke is single use and logout all scoped to user`(): Unit = runBlocking {
        val (raw, token) = setup()
        assertTrue(tokens.revokeIfActive(token.id, clock.instant()))
        assertFalse(tokens.revokeIfActive(token.id, clock.instant()))
        assertFailsWith<AuthenticationException> { useCase()(raw) }
        val now = clock.instant()
        val otherUser = User(UUID.randomUUID(), "other@test.com", "+5355550101", "hash", "Other", "User",
            setOf(Role.CUSTOMER), true, now, now)
        users.save(otherUser)
        val otherToken = token.copy(id = UUID.randomUUID(), userId = otherUser.id, tokenHash = generator.hash(generator.generate()), revokedAt = null)
        val active = token.copy(id = UUID.randomUUID(), tokenHash = generator.hash(generator.generate()), revokedAt = null)
        tokens.save(otherToken)
        tokens.save(active)
        tokens.revokeAllByUser(token.userId, now)
        assertNotNull(tokens.findByHash(active.tokenHash)?.revokedAt)
        assertNull(tokens.findByHash(otherToken.tokenHash)?.revokedAt)
    }
}

