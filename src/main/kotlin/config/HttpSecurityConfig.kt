package com.blackneko.config

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.plugins.forwardedheaders.*
import io.ktor.server.plugins.origin
import io.ktor.server.request.*
import java.util.UUID
import kotlin.time.Duration.Companion.minutes

val AUTH_RATE_LIMIT = RateLimitName("auth")

fun Application.configureHttpSecurity(authLimit: Int = 20) {
    if (environment.config.propertyOrNull("http.trustProxy")?.getString()?.toBooleanStrictOrNull() == true) {
        install(XForwardedHeaders)
    }
    install(RateLimit) {
        register(AUTH_RATE_LIMIT) {
            rateLimiter(limit = authLimit, refillPeriod = 1.minutes)
            // Forwarded headers are only enabled behind the explicitly trusted private proxy.
            requestKey { it.request.origin.remoteHost }
        }
    }
    install(CORS) {
        val hosts = this@configureHttpSecurity.environment.config.propertyOrNull("cors.allowedHosts")?.getString().orEmpty()
        hosts.split(',').map(String::trim).filter(String::isNotEmpty).forEach {
            allowHost(it, schemes = listOf("https", "http"))
        }
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowHeader("Idempotency-Key")
        exposeHeader("X-Request-ID")
        exposeHeader("Retry-After")
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
    }
    install(CallId) {
        generate { UUID.randomUUID().toString() }
        replyToHeader("X-Request-ID")
    }
    install(CallLogging) {
        format { "${it.request.httpMethod.value} ${it.request.path()} ${it.response.status()?.value} requestId=${it.callId}" }
    }
}
