package com.blackneko.infrastructure.database.mapper

import com.blackneko.domain.shared.Money

fun Long.toMoney() =
    Money(this)

fun Money.toLong() =
    cents