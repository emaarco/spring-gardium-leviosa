package de.emaarco.podbroadcast

import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.kafka.core.KafkaTemplate
import tools.jackson.databind.json.JsonMapper

/**
 * Sends an event to all pods of this service, including the pod that sends it.
 * Events with the same key keep their order. Returns once Kafka has acknowledged the event.
 */
class PodBroadcastPublisher(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val jsonMapper: JsonMapper,
    private val topic: String,
) {

    fun publishToAllPods(
        key: String,
        event: Any,
    ) {
        val record = ProducerRecord(topic, key, jsonMapper.writeValueAsString(event))
        record.headers().add(PodBroadcastHeaders.EVENT_TYPE, event.javaClass.name.toByteArray())
        kafkaTemplate.send(record).join()
    }
}
