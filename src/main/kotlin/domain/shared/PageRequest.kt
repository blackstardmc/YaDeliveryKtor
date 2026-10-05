package com.blackneko.domain.shared

data class PageRequest(val limit: Int = 20, val offset: Int = 0) {
    init {
        require(limit in 1..100 && offset in 0..100_000) { "Invalid pagination" }
    }
}
