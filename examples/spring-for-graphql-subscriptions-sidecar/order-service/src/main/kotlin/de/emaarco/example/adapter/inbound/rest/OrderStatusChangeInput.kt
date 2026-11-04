package de.emaarco.example.adapter.inbound.rest

import de.emaarco.example.domain.OrderId
import de.emaarco.example.domain.OrderStatus
import de.emaarco.example.domain.OrderStatusChange
import java.time.Instant

data class OrderStatusChangeInput(
    val orderId: String,
    val status: String,
    val changedAt: String,
    val changedOnPod: String,
) {
    fun toDomain() =
        OrderStatusChange(
            orderId = OrderId(orderId),
            status = OrderStatus.valueOf(status),
            changedAt = Instant.parse(changedAt),
            changedOnPod = changedOnPod,
        )
}
