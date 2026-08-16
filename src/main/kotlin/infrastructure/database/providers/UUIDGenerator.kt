package com.blackneko.infrastructure.database.providers

import java.util.UUID

class UUIDGenerator : IdGenerator {

    override fun generate(): UUID =
        UUID.randomUUID()
}

interface IdGenerator {

    fun generate(): UUID
}