package de.emaarco.example.application.service

import de.emaarco.example.application.port.inbound.ChangeOrderStatusUseCase
import de.emaarco.example.application.port.outbound.OrderStatusBroadcastPort
import de.emaarco.example.domain.FulfillmentUpdate
import de.emaarco.example.domain.OrderStatusChange
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class ChangeOrderStatusService(
    private val broadcast: OrderStatusBroadcastPort,
    @Value("\${pod.name}") private val podName: String,
) : ChangeOrderStatusUseCase {

    override fun changeOrderStatus(update: FulfillmentUpdate) {
        val change = OrderStatusChange(update.orderId, update.status, Instant.now(), podName)
        broadcast.broadcastToAllPods(change)
    }
}
