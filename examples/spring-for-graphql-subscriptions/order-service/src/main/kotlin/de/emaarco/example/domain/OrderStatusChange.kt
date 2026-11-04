package de.emaarco.example.domain

import java.time.Instant

data class OrderStatusChange(
    val orderId: OrderId,
    val status: OrderStatus,
    val changedAt: Instant,
    val changedOnPod: String,
)
