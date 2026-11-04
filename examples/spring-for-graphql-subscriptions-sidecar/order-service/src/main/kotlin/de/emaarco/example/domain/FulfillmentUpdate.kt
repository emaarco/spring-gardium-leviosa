package de.emaarco.example.domain

data class FulfillmentUpdate(
    val orderId: OrderId,
    val status: OrderStatus,
)
