package de.emaarco.example.adapter.outbound

import de.emaarco.example.domain.FulfillmentUpdate
import de.emaarco.example.domain.Order
import de.emaarco.example.domain.OrderId
import de.emaarco.example.domain.OrderStatus
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.atMost
import org.awaitility.kotlin.await
import org.awaitility.kotlin.until
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.context.ApplicationEventPublisher
import java.time.Duration
import java.util.concurrent.CopyOnWriteArrayList

class SimulatedOrderFulfillmentAdapterTest {

    private val reportedUpdates = CopyOnWriteArrayList<Any>()
    private val adapter =
        SimulatedOrderFulfillmentAdapter(ApplicationEventPublisher { reportedUpdates.add(it) }, Duration.ofMillis(10))

    private val order = Order(OrderId("order-42"), "Espresso machine", 1, OrderStatus.PLACED, "pod-2")

    @AfterEach
    fun stopFulfillment() {
        adapter.destroy()
    }

    @Test
    fun `reports payment, readiness to ship and shipping one after another`() {
        adapter.startFulfillment(order)

        await atMost Duration.ofSeconds(2) until { reportedUpdates.size == 3 }
        assertThat(reportedUpdates).containsExactly(
            FulfillmentUpdate(order.id, OrderStatus.PAYMENT_CONFIRMED),
            FulfillmentUpdate(order.id, OrderStatus.READY_TO_SHIP),
            FulfillmentUpdate(order.id, OrderStatus.SHIPPED),
        )
    }
}
