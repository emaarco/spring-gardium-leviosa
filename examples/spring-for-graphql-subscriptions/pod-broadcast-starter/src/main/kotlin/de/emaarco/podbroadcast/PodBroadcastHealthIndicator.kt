package de.emaarco.podbroadcast

import org.springframework.boot.health.contributor.Health
import org.springframework.boot.health.contributor.HealthIndicator
import org.springframework.kafka.config.KafkaListenerEndpointRegistry

class PodBroadcastHealthIndicator(
    private val registry: KafkaListenerEndpointRegistry,
) : HealthIndicator {

    override fun health(): Health {
        val listener = registry.getListenerContainer(PodBroadcastListener.LISTENER_ID)
        val assignedPartitions = listener?.assignedPartitions.orEmpty()
        if (assignedPartitions.isEmpty()) {
            return Health.down().withDetail("reason", "waiting for the partitions of the broadcast topic").build()
        }
        return Health.up().withDetail("assignedPartitions", assignedPartitions.size).build()
    }
}
