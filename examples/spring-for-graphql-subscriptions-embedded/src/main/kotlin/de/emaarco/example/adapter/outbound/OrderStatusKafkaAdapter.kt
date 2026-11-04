package de.emaarco.example.adapter.outbound

import de.emaarco.example.application.port.outbound.OrderStatusBroadcastPort
import de.emaarco.example.domain.OrderStatusChange
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import tools.jackson.databind.json.JsonMapper

@Component
class OrderStatusKafkaAdapter(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val jsonMapper: JsonMapper,
    @Value("\${order-status-changes.topic}") private val topic: String,
) : OrderStatusBroadcastPort {

    private val log = KotlinLogging.logger {}

    override fun broadcastToAllPods(change: OrderStatusChange) {
        val payload = jsonMapper.writeValueAsString(OrderStatusChangeMessage.from(change))
        kafkaTemplate.send(topic, change.orderId.value, payload).join()
        log.info { "Broadcast status ${change.status} of order ${change.orderId.value} to all pods via topic $topic" }
    }

    data class OrderStatusChangeMessage(
        val orderId: String,
        val status: String,
        val changedAt: String,
        val changedOnPod: String,
    ) {
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
