package de.emaarco.example.application.service

import de.emaarco.example.application.port.inbound.PlaceOrderUseCase
import de.emaarco.example.application.port.outbound.OrderFulfillmentPort
import de.emaarco.example.domain.Order
import de.emaarco.example.domain.OrderId
import de.emaarco.example.domain.OrderStatus
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class PlaceOrderService(
    private val fulfillment: OrderFulfillmentPort,
    @Value("\${pod.name}") private val podName: String,
) : PlaceOrderUseCase {

    override fun placeOrder(
        article: String,
        quantity: Int,
    ): Order {
        val order = Order(OrderId(UUID.randomUUID().toString()), article, quantity, OrderStatus.PLACED, podName)
        fulfillment.startFulfillment(order)
        return order
    }
}
