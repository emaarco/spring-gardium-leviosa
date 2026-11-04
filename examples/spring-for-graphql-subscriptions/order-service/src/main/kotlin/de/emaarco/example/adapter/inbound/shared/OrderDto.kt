package de.emaarco.example.adapter.inbound.shared

import de.emaarco.example.domain.Order

data class OrderDto(
    val id: String,
    val article: String,
    val quantity: Int,
    val status: String,
    val placedOnPod: String,
) {
    companion object {
        fun from(order: Order) =
            OrderDto(
                id = order.id.value,
                article = order.article,
                quantity = order.quantity,
                status = order.status.name,
                placedOnPod = order.placedOnPod,
            )
    }
}
