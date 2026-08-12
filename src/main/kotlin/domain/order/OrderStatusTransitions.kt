package com.blackneko.domain.order

object OrderStatusTransitions {

    private val transitions =
        mapOf(

            OrderStatus.RECEIVED to setOf(
                OrderStatus.CONFIRMED,
                OrderStatus.CANCELLED
            ),

            OrderStatus.CONFIRMED to setOf(
                OrderStatus.PREPARING,
                OrderStatus.CANCELLED
            ),

            OrderStatus.PREPARING to setOf(
                OrderStatus.READY,
                OrderStatus.CANCELLED
            ),

            OrderStatus.READY to setOf(
                OrderStatus.IN_DELIVERY
            ),

            OrderStatus.IN_DELIVERY to setOf(
                OrderStatus.DELIVERED
            ),

            OrderStatus.DELIVERED to emptySet(),

            OrderStatus.CANCELLED to emptySet()
        )

    fun canTransition(
        from: OrderStatus,
        to: OrderStatus
    ): Boolean {

        return transitions[from]
            ?.contains(to)
            ?: false
    }
}