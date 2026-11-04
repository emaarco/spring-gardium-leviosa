package de.emaarco.sidecar.adapter.inbound.kafka

import de.emaarco.sidecar.application.port.inbound.DeliverEventUseCase
import de.emaarco.sidecar.domain.Event
import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class BrokerEventListener(
    private val useCase: DeliverEventUseCase,
) {

    private val log = KotlinLogging.logger {}

    @KafkaListener(
        id = LISTENER_ID,
        topics = ["\${relay.topic}"],
        groupId = "\${spring.application.name}-\${pod.name}",
    )
    fun onEvent(record: ConsumerRecord<String, String>) {
        log.info { "Received event with key ${record.key()} from partition ${record.partition()}" }
        useCase.deliver(Event(record.key(), record.value()))
    }

    companion object {
        const val LISTENER_ID = "relay"
    }
}
