package de.emaarco.sidecar.adapter.outbound

import de.emaarco.sidecar.application.port.outbound.MessageBrokerPort
import de.emaarco.sidecar.domain.Event
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class KafkaBrokerAdapter(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    @Value("\${relay.topic}") private val topic: String,
) : MessageBrokerPort {
    override fun publishToAllPods(event: Event) {
        kafkaTemplate.send(ProducerRecord(topic, event.key, event.payload)).join()
    }
}
