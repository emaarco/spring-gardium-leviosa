package de.emaarco.sidecar.adapter.inbound.kafka

import org.springframework.boot.health.contributor.Health
import org.springframework.boot.health.contributor.HealthIndicator
import org.springframework.kafka.config.KafkaListenerEndpointRegistry
import org.springframework.stereotype.Component

@Component
class BrokerEventListenerHealthIndicator(
    private val registry: KafkaListenerEndpointRegistry,
) : HealthIndicator {

    override fun health(): Health {
        val listener = registry.getListenerContainer(BrokerEventListener.LISTENER_ID)
        val assignedPartitions = listener?.assignedPartitions.orEmpty()
        if (assignedPartitions.isEmpty()) {
            return Health.down().withDetail("reason", "waiting for the partitions of the relayed topic").build()
        }
        return Health.up().withDetail("assignedPartitions", assignedPartitions.size).build()
    }
}
