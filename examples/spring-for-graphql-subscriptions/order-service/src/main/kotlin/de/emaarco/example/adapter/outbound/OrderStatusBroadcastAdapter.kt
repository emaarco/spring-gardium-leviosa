package de.emaarco.example.adapter.outbound

import de.emaarco.example.application.port.outbound.OrderStatusBroadcastPort
import de.emaarco.example.domain.OrderId
import de.emaarco.example.domain.OrderStatus
import de.emaarco.example.domain.OrderStatusChange
import de.emaarco.podbroadcast.PodBroadcaster
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class OrderStatusBroadcastAdapter(
    private val broadcaster: PodBroadcaster,
) : OrderStatusBroadcastPort {

    private val log = KotlinLogging.logger {}

    override fun broadcastToAllPods(change: OrderStatusChange) {
        broadcaster.publishToAllPods(change.orderId.value, OrderStatusChangeMessage.from(change))
        log.info { "Broadcast status ${change.status} of order ${change.orderId.value} to all pods" }
    }

    override fun changesOf(orderId: OrderId) =
        broadcaster
            .receive<OrderStatusChangeMessage>()
            .filter { it.orderId == orderId.value }
            .map { it.toDomain() }

    data class OrderStatusChangeMessage(
        val orderId: String,
        val status: String,
        val changedAt: String,
        val changedOnPod: String,
    ) {
        fun toDomain() =
            OrderStatusChange(
                orderId = OrderId(orderId),
                status = OrderStatus.valueOf(status),
                changedAt = Instant.parse(changedAt),
                changedOnPod = changedOnPod,
            )

        companion object {
            fun from(change: OrderStatusChange) =
                OrderStatusChangeMessage(
                    orderId = change.orderId.value,
                    status = change.status.name,
                    changedAt = change.changedAt.toString(),
                    changedOnPod = change.changedOnPod,
                )
        }
    }
}
