package de.emaarco.example.adapter.inbound.shared

import de.emaarco.example.domain.OrderStatusChange

data class OrderStatusChangeDto(
    val orderId: String,
    val status: String,
    val changedAt: String,
    val changedOnPod: String,
) {
    companion object {
        fun from(change: OrderStatusChange) =
            OrderStatusChangeDto(
                orderId = change.orderId.value,
                status = change.status.name,
                changedAt = change.changedAt.toString(),
                changedOnPod = change.changedOnPod,
            )
    }
}
