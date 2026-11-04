package de.emaarco.example.application.port.inbound

import de.emaarco.example.domain.Order

interface PlaceOrderUseCase {
    fun placeOrder(
        article: String,
        quantity: Int,
    ): Order
}
