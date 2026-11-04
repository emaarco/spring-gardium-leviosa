package de.emaarco.example.adapter.inbound.kafka

import de.emaarco.example.application.port.inbound.NotifyOrderSubscribersUseCase
import de.emaarco.example.domain.OrderId
import de.emaarco.example.domain.OrderStatus
import de.emaarco.example.domain.OrderStatusChange
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import tools.jackson.databind.json.JsonMapper
import java.time.Instant

@Component
class OrderStatusKafkaListener(
    private val useCase: NotifyOrderSubscribersUseCase,
    private val jsonMapper: JsonMapper,
) {

    private val log = KotlinLogging.logger {}

    @KafkaListener(
        id = LISTENER_ID,
        topics = ["\${order-status-changes.topic}"],
        groupId = "\${spring.application.name}-\${pod.name}",
    )
    fun onOrderStatusChange(payload: String) {
        val message = jsonMapper.readValue(payload, OrderStatusChangeMessage::class.java)
        log.info { "Received status ${message.status} of order ${message.orderId} changed on ${message.changedOnPod}" }
        useCase.notifySubscribers(message.toDomain())
    }

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
    }

    companion object {
        const val LISTENER_ID = "order-status-changes"
    }
}
