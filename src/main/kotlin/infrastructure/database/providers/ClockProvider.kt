package com.blackneko.infrastructure.database.providers

import java.time.Instant

interface ClockProvider {

    fun now(): Instant
}

class SystemClockProvider : ClockProvider {
    override fun now(): Instant = Instant.now()
}