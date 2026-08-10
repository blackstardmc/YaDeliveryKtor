package com.blackneko.domain.order

enum class OrderStatus {
    RECEIVED,
    CONFIRMED,
    PREPARING,
    READY,
    IN_DELIVERY,
    DELIVERED,
    CANCELLED
}

fun OrderStatus.canTransitionTo(
    target: OrderStatus
): Boolean {

    return when (this) {

        OrderStatus.RECEIVED ->
            target in setOf(
                OrderStatus.CONFIRMED,
                OrderStatus.CANCELLED
            )

        OrderStatus.CONFIRMED ->
            target in setOf(
                OrderStatus.PREPARING,
                OrderStatus.CANCELLED
            )

        OrderStatus.PREPARING ->
            target in setOf(
                OrderStatus.READY,
                OrderStatus.CANCELLED
            )

        OrderStatus.READY ->
            target == OrderStatus.IN_DELIVERY

        OrderStatus.IN_DELIVERY ->
            target == OrderStatus.DELIVERED

        OrderStatus.DELIVERED ->
            false

        OrderStatus.CANCELLED ->
            false
    }
}