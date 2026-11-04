package de.emaarco.example.application.service

import de.emaarco.example.application.port.outbound.OrderStatusBroadcastPort
import de.emaarco.example.domain.FulfillmentUpdate
import de.emaarco.example.domain.OrderId
import de.emaarco.example.domain.OrderStatus
import de.emaarco.example.domain.OrderStatusChange
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.slot
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ChangeOrderStatusServiceTest {

    private val broadcast = mockk<OrderStatusBroadcastPort>()
    private val service = ChangeOrderStatusService(broadcast, "pod-2")

    @Test
    fun `broadcasts the status change to all pods instead of notifying local subscribers directly`() {
        val broadcastChange = slot<OrderStatusChange>()
        justRun { broadcast.broadcastToAllPods(capture(broadcastChange)) }

        service.changeOrderStatus(FulfillmentUpdate(OrderId("order-42"), OrderStatus.SHIPPED))

        assertThat(broadcastChange.captured.orderId).isEqualTo(OrderId("order-42"))
        assertThat(broadcastChange.captured.status).isEqualTo(OrderStatus.SHIPPED)
        assertThat(broadcastChange.captured.changedOnPod).isEqualTo("pod-2")
    }
}
