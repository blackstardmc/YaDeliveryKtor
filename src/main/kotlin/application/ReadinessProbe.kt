package com.blackneko.application

fun interface ReadinessProbe {
    suspend fun isReady(): Boolean
}
