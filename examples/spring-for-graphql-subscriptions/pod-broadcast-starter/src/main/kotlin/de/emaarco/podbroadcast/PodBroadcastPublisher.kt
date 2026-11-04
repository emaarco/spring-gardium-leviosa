package de.emaarco.podbroadcast

import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.kafka.core.KafkaTemplate
import tools.jackson.databind.json.JsonMapper

/**
 * Sends an event to all pods of this service, including the pod that sends it.
 * Events with the same key keep their order. Blocks until Kafka has acknowledged the event, so a lost event fails loudly.
 * The simple class name of the event is its type on the wire: keep it stable across releases.
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
        record.headers().add(PodBroadcastHeaders.EVENT_TYPE, event.javaClass.simpleName.toByteArray())
        kafkaTemplate.send(record).join()
    }
}
