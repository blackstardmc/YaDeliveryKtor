package com.blackneko.application

import java.util.UUID

/** Locks are held until the enclosing TransactionRunner transaction finishes. */
interface EntityLocks {
    suspend fun user(id: UUID)
    suspend fun order(id: UUID)
    suspend fun driver(userId: UUID)
    suspend fun restaurant(id: UUID)
}
