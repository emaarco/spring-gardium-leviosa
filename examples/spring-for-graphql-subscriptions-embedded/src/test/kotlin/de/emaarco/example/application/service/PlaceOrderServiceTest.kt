package de.emaarco.example.application.service

import de.emaarco.example.application.port.outbound.OrderFulfillmentPort
import de.emaarco.example.domain.OrderStatus
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PlaceOrderServiceTest {

    private val fulfillment = mockk<OrderFulfillmentPort>()
    private val service = PlaceOrderService(fulfillment, "pod-2")

    @Test
    fun `places the order on this pod and starts its fulfillment`() {
        justRun { fulfillment.startFulfillment(any()) }

        val order = service.placeOrder("Espresso machine", 1)

        assertThat(order.status).isEqualTo(OrderStatus.PLACED)
        assertThat(order.placedOnPod).isEqualTo("pod-2")
        verify(exactly = 1) { fulfillment.startFulfillment(order) }
    }

    @Test
    fun `assigns a new id to every order`() {
        justRun { fulfillment.startFulfillment(any()) }

        val first = service.placeOrder("Espresso machine", 1)
        val second = service.placeOrder("Espresso machine", 1)

        assertThat(first.id).isNotEqualTo(second.id)
    }
}
