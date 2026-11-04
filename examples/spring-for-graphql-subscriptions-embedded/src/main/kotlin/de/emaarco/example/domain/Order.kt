package de.emaarco.example.domain

data class Order(
    val id: OrderId,
    val article: String,
    val quantity: Int,
    val status: OrderStatus,
    val placedOnPod: String,
)
